#include <inttypes.h>
#include <math.h>
#include <unistd.h>
#include <limits.h>
#include <signal.h>
#include "libavutil/avstring.h"
#include "libavutil/colorspace.h"
#include "libavutil/mathematics.h"
#include "libavutil/pixdesc.h"
#include "libavutil/imgutils.h"
#include "libavutil/dict.h"
#include "libavutil/parseutils.h"
#include "libavutil/samplefmt.h"
#include "libavutil/avassert.h"
#include "libavformat/avformat.h"
#include "libavdevice/avdevice.h"
#include "libswscale/swscale.h"
#include "libavutil/opt.h"
#include "libavcodec/avfft.h"
#include "libswresample/swresample.h"

#include <android/log.h>
#include <SDL.h>
#include <SDL_thread.h>
#include <SDL_events.h>

#include <unistd.h>
#include <assert.h>

#define MAX_QUEUE_SIZE (15 * 1024 * 1024)
//Min Frame is 10sec frame
#define MIN_FRAMES (10*15)
//Max Frame is 3min video frame
#define MAX_FRAMES (3*60*15)

#define MSG_LOAD_FINISHED 		10
#define MSG_LOAD_UNFINISHED     11
#define MSG_OPEN_ERROR			12
#define MSG_OPEN_OK				13

#define RET_OK 					0
#define RET_INIT_ERROR			-1
#define RET_PREPARE_ERROR		-2
#define RET_FILE_NOTEXIST		-3
#define RET_NETWORK_ERROR		-4
#define RET_FILE_OPENERROR		-5
#define RET_FORMAT_NOT_SUPPORT	-6
#define RET_STREAM_OPEN_FAIL	-7

#define SKIP_DELAY_TIME			0.3

/* SDL audio buffer size, in samples. Should be small to have precise
 A/V sync as SDL does not have hardware buffer fullness info. */
#define SDL_AUDIO_BUFFER_SIZE 1024

/* no AV sync correction is done if below the AV sync threshold */
#define AV_SYNC_THRESHOLD 0.01
/* no AV correction is done if too big error */
#define AV_NOSYNC_THRESHOLD 10.0

/* maximum audio speed change to get correct sync */
#define SAMPLE_CORRECTION_PERCENT_MAX 10

/* we use about AUDIO_DIFF_AVG_NB A-V differences to make the average */
#define AUDIO_DIFF_AVG_NB   20

/* NOTE: the size must be big enough to compensate the hardware audio buffersize size */
#define SAMPLE_ARRAY_SIZE (2 * 65536)
#define SDL_EVENTMASK(X)    (1<<(X))

static int sws_flags = SWS_FAST_BILINEAR; //SWS_BICUBIC;
static int dst_fix_fmt = PIX_FMT_RGB565;
static int display_fix_fmt = SDL_PIXELFORMAT_RGB565;

typedef struct PacketQueue {
	AVPacketList *first_pkt, *last_pkt;
	int nb_packets;
	int size;
	int abort_request;
	SDL_mutex *mutex;
	SDL_cond *cond;
} PacketQueue;

#define VIDEO_PICTURE_QUEUE_SIZE 2
#define SUBPICTURE_QUEUE_SIZE 4

typedef struct VideoPicture {
	double pts; ///< presentation time stamp for this picture
	double duration; ///< expected duration of the frame
	int64_t pos; ///< byte position in file
	int skip;

	AVFrame *pFrameRGB;
	int numBytes;
	uint8_t *buffer;

	int width, height; /* source height & width */
	AVRational sample_aspect_ratio;
	int allocated;
	int reallocate;
	enum PixelFormat pix_fmt;

} VideoPicture;

enum {
	AV_SYNC_AUDIO_MASTER, /* default choice */
	AV_SYNC_VIDEO_MASTER, AV_SYNC_EXTERNAL_CLOCK, /* synchronize to an external clock */
};

enum ShowMode {
	SHOW_MODE_NONE = -1,
	SHOW_MODE_VIDEO = 0,
	SHOW_MODE_WAVES,
	SHOW_MODE_RDFT,
	SHOW_MODE_NB
};

typedef struct VideoState {
	SDL_Thread *read_tid;
	SDL_Thread *video_tid;
	SDL_Thread *refresh_tid;
	AVInputFormat *iformat;
	int no_background;
	int abort_request;
	int force_refresh;
	int paused;
	int last_paused;
	int seek_req;
	int seek_flags;
	int64_t seek_pos;
	int64_t seek_rel;
	int read_pause_return;
	AVFormatContext *ic;

	int audio_stream;

	int av_sync_type;
	double external_clock; /* external clock base */
	int64_t external_clock_time;

	double audio_clock;
	double audio_diff_cum; /* used for AV difference average computation */
	double audio_diff_avg_coef;
	double audio_diff_threshold;
	int audio_diff_avg_count;
	AVStream *audio_st;
	PacketQueue audioq;
	int audio_hw_buf_size;
	DECLARE_ALIGNED(16,uint8_t,audio_buf2) [AVCODEC_MAX_AUDIO_FRAME_SIZE * 4];
	uint8_t silence_buf[SDL_AUDIO_BUFFER_SIZE];
	uint8_t *audio_buf;
	uint8_t *audio_buf1;
	unsigned int audio_buf_size; /* in bytes */
	int audio_buf_index; /* in bytes */
	int audio_write_buf_size;
	AVPacket audio_pkt_temp;
	AVPacket audio_pkt;
	enum AVSampleFormat audio_src_fmt;
	enum AVSampleFormat audio_tgt_fmt;
	int audio_src_channels;
	int audio_tgt_channels;
	int64_t audio_src_channel_layout;
	int64_t audio_tgt_channel_layout;
	int audio_src_freq;
	int audio_tgt_freq;
	struct SwrContext *swr_ctx;
	double audio_current_pts;
	double audio_current_pts_drift;
	int frame_drops_early;
	int frame_drops_late;
	AVFrame *frame;

	enum ShowMode show_mode;
	int16_t sample_array[SAMPLE_ARRAY_SIZE];
	int sample_array_index;
	int last_i_start;
	RDFTContext *rdft;
	int rdft_bits;
	FFTSample *rdft_data;
	int xpos;

	double frame_timer;
	double frame_last_pts;
	double frame_last_duration;
	double frame_last_dropped_pts;
	double frame_last_returned_time;
	double frame_last_filter_delay;
	int64_t frame_last_dropped_pos;
	double video_clock; ///< pts of last decoded frame / predicted pts of next decoded frame
	int video_stream;
	AVStream *video_st;
	PacketQueue videoq;
	double video_current_pts; ///< current displayed pts (different from video_clock if frame fifos are used)
	double video_current_pts_drift; ///< video_current_pts - time (av_gettime) at which we updated video_current_pts - used to have running video pts
	int64_t video_current_pos; ///< current displayed file pos
	VideoPicture pictq[VIDEO_PICTURE_QUEUE_SIZE];
	int pictq_size, pictq_rindex, pictq_windex;
	SDL_mutex *pictq_mutex;
	SDL_cond *pictq_cond;
	struct SwsContext *img_convert_ctx;

	char filename[1024];
	int width, height, xleft, ytop;
	int step;

	int refresh;
	int last_video_stream, last_audio_stream, last_subtitle_stream;
	int load;
} VideoState;

typedef struct AllocEventProps {
	VideoState *is;
	AVFrame *frame;
} AllocEventProps;

/* options specified by the user */
static const char *input_filename;
static int fs_screen_width = 0;
static int fs_screen_height = 0;
static int screen_width = 0;
static int screen_height = 0;
static int audio_disable;
static int video_disable;
static int wanted_stream[AVMEDIA_TYPE_NB] = { [AVMEDIA_TYPE_AUDIO] = -1,
		[AVMEDIA_TYPE_VIDEO] = -1, [AVMEDIA_TYPE_SUBTITLE] = -1, };
static int seek_by_bytes = -1;
static int display_disable;
static int show_status = 1;
static int av_sync_type = AV_SYNC_AUDIO_MASTER;
static int64_t start_time = AV_NOPTS_VALUE;
static int64_t duration = AV_NOPTS_VALUE;
static int workaround_bugs = 1;
static int genpts = 0;
static int lowres = 0;
static int idct = FF_IDCT_AUTO;
static enum AVDiscard skip_frame = AVDISCARD_DEFAULT;
static enum AVDiscard skip_idct = AVDISCARD_DEFAULT;
static enum AVDiscard skip_loop_filter = AVDISCARD_DEFAULT;
static int error_concealment = 3;
static int decoder_reorder_pts = -1;
static int autoexit;
static int exit_on_keydown;
static int exit_on_mousedown;
static int loop = 1;
static int framedrop = -1;
static enum ShowMode show_mode = SHOW_MODE_NONE;
static const char *audio_codec_name;
static const char *subtitle_codec_name;
static const char *video_codec_name;
static int rdftspeed = 20;
static VideoState *g_stream = NULL;

/* current context */
static int is_full_screen = 1;
static int64_t audio_callback_time;

static AVPacket flush_pkt;

#define FF_ALLOC_EVENT   (SDL_USEREVENT)
#define FF_REFRESH_EVENT (SDL_USEREVENT + 1)
#define FF_QUIT_EVENT    (SDL_USEREVENT + 2)
#define KU_PLAYER_SEEK   (SDL_USEREVENT	+ 3)
#define KU_PLAYER_PAUSE  (SDL_USEREVENT	+ 4)
#define KU_PLAYER_BUFCHK (SDL_USEREVENT	+ 5)

static SDL_Window *window = NULL;
static SDL_Renderer *renderer = NULL;
static SDL_Texture *texture = NULL;

static int64_t g_total_duration = 0; //msec
static int g_current_duration = 0; //msec
static int g_skip = 1;

void print_error(const char *filename, int err) {
	char errbuf[128];
	const char *errbuf_ptr = errbuf;

	if (av_strerror(err, errbuf, sizeof(errbuf)) < 0)
		errbuf_ptr = strerror(AVUNERROR(err));
	av_log(NULL, AV_LOG_ERROR, "%s: %s\n", filename, errbuf_ptr);
}

static int packet_queue_put_private(PacketQueue *q, AVPacket *pkt) {
	AVPacketList *pkt1;

	if (q->abort_request)
		return -1;

	pkt1 = av_malloc(sizeof(AVPacketList));
	if (!pkt1)
		return -1;
	pkt1->pkt = *pkt;
	pkt1->next = NULL;

	if (!q->last_pkt)
		q->first_pkt = pkt1;
	else
		q->last_pkt->next = pkt1;
	q->last_pkt = pkt1;
	q->nb_packets++;
	q->size += pkt1->pkt.size + sizeof(*pkt1);
	/* XXX: should duplicate packet data in DV case */
	SDL_CondSignal(q->cond);
	return 0;
}

static int packet_queue_put(PacketQueue *q, AVPacket *pkt) {
	int ret;
	/* duplicate the packet */
	if (pkt != &flush_pkt && av_dup_packet(pkt) < 0)
		return -1;

	SDL_LockMutex(q->mutex);
	ret = packet_queue_put_private(q, pkt);
	SDL_UnlockMutex(q->mutex);

	if (pkt != &flush_pkt && ret < 0)
		av_free_packet(pkt);
	return ret;
}

/* packet queue handling */
static void packet_queue_init(PacketQueue *q) {
	memset(q, 0, sizeof(PacketQueue));
	q->mutex = SDL_CreateMutex();
	q->cond = SDL_CreateCond();
	q->abort_request = 1;
}

static void packet_queue_flush(PacketQueue *q) {
	AVPacketList *pkt, *pkt1;

	SDL_LockMutex(q->mutex);
	for (pkt = q->first_pkt; pkt != NULL; pkt = pkt1) {
		pkt1 = pkt->next;
		av_free_packet(&pkt->pkt);
		av_freep(&pkt);
	}
	q->last_pkt = NULL;
	q->first_pkt = NULL;
	q->nb_packets = 0;
	q->size = 0;
	SDL_UnlockMutex(q->mutex);
}

static void packet_queue_destroy(PacketQueue *q) {
	packet_queue_flush(q);
	SDL_DestroyMutex(q->mutex);
	SDL_DestroyCond(q->cond);
}

static void packet_queue_abort(PacketQueue *q) {
	SDL_LockMutex(q->mutex);

	q->abort_request = 1;

	SDL_CondSignal(q->cond);

	SDL_UnlockMutex(q->mutex);
}

static void packet_queue_start(PacketQueue *q) {
	SDL_LockMutex(q->mutex);
	q->abort_request = 0;
	packet_queue_put_private(q, &flush_pkt);
	SDL_UnlockMutex(q->mutex);
}

/* return < 0 if aborted, 0 if no packet and > 0 if packet.  */
static int packet_queue_get(PacketQueue *q, AVPacket *pkt, int block) {
	AVPacketList *pkt1;
	int ret;

	SDL_LockMutex(q->mutex);

	for (;;) {
		if (q->abort_request) {
			ret = -1;
			break;
		}

		pkt1 = q->first_pkt;
		if (pkt1) {
			q->first_pkt = pkt1->next;
			if (!q->first_pkt)
				q->last_pkt = NULL;
			q->nb_packets--;
			q->size -= pkt1->pkt.size + sizeof(*pkt1);
			*pkt = pkt1->pkt;
			av_free(pkt1);
			ret = 1;
			break;
		} else if (!block) {
			ret = 0;
			break;
		} else {
			SDL_CondWait(q->cond, q->mutex);
		}
	}
	SDL_UnlockMutex(q->mutex);
	return ret;
}

static inline int check_buffer(VideoState *is, int packet) {
	if (is->videoq.nb_packets < packet)
		return 0;
	else
		return 1;
}

static double get_audio_clock(VideoState *is) {
	if (is->paused) {
		return is->audio_current_pts;
	} else {
		return is->audio_current_pts_drift + av_gettime() / 1000000.0;
	}
}

/* get the current video clock value */
static double get_video_clock(VideoState *is) {
	if (is->paused) {
		return is->video_current_pts;
	} else {
		return is->video_current_pts_drift + av_gettime() / 1000000.0;
	}
}

/* get the current external clock value */
static double get_external_clock(VideoState *is) {
	int64_t ti;
	ti = av_gettime();
	return is->external_clock + ((ti - is->external_clock_time) * 1e-6);
}

/* get the current master clock value */
static double get_master_clock(VideoState *is) {
	double val;

	if (is->av_sync_type == AV_SYNC_VIDEO_MASTER) {
		if (is->video_st)
			val = get_video_clock(is);
		else
			val = get_audio_clock(is);
	} else if (is->av_sync_type == AV_SYNC_AUDIO_MASTER) {
		if (is->audio_st)
			val = get_audio_clock(is);
		else
			val = get_video_clock(is);
	} else {
		val = get_external_clock(is);
	}
	return val;
}
int change = 0;
int mWidth, mHeight;
void is_change(int width, int height) {
	mWidth = width;
	mHeight = height;
	change = 1;
}

static void video_image_display(VideoState *is) {
	VideoPicture *vp;
	float aspect_ratio;
	int width, height, x, y;
	SDL_Rect rect;

	if (change == 1) {
		change = 0;
		SDL_Rect windowRect;
		windowRect.x = 0;
		windowRect.y = 0;
		windowRect.w = mWidth;
		windowRect.h = mHeight;
//		使用这个函数来设置绘图区域呈现当前目标。
		SDL_RenderSetViewport(renderer, &windowRect);
		is->width = mWidth;
		is->height = mHeight;
		SDL_Delay(10);
	}

	vp = &is->pictq[is->pictq_rindex];
	if (vp->pFrameRGB) {
		if (vp->sample_aspect_ratio.num == 0)
			aspect_ratio = 0;
		else
			aspect_ratio = av_q2d(vp->sample_aspect_ratio);

		if (aspect_ratio <= 0.0)
			aspect_ratio = 1.0;
//		取得高宽比,分辨率如240X320
		aspect_ratio *= (float) vp->width / (float) vp->height;

		height = is->height;
		width = ((int) rint(height * aspect_ratio)) & ~1;
		if (width > is->width) {
			width = is->width;
			height = ((int) rint(width / aspect_ratio)) & ~1;
		}
		x = (is->width - width) / 2;
		y = (is->height - height) / 2;
		is->no_background = 0;
		rect.x = is->xleft + x;
		rect.y = is->ytop + y;
		rect.w = FFMAX(width, 1);
		rect.h = FFMAX(height, 1);

		if (NULL == texture) {
			texture = SDL_CreateTexture(renderer, display_fix_fmt,
					SDL_TEXTUREACCESS_STATIC, is->video_st->codec->width,
					is->video_st->codec->height);
			if (!texture) {
				exit(1);
			}
			SDL_SetTextureBlendMode(texture, SDL_BLENDMODE_BLEND);
		}
		SDL_RenderClear(renderer);
		SDL_UpdateTexture(texture, NULL, vp->pFrameRGB->data[0],
				vp->pFrameRGB->linesize[0]);
		SDL_RenderCopy(renderer, texture, NULL, &rect);

		g_current_duration = (int) get_master_clock(is) * 1000;
		SDL_RenderPresent(renderer);
	}
}

static void stream_close(VideoState *is) {
	VideoPicture *vp;
	int i;
	/* XXX: use a special url_shutdown call to abort parse cleanly */
	is->abort_request = 1;
	SDL_WaitThread(is->read_tid, NULL);
	SDL_WaitThread(is->refresh_tid, NULL);
	packet_queue_destroy(&is->videoq);
	packet_queue_destroy(&is->audioq);

	/* free all pictures */
	for (i = 0; i < VIDEO_PICTURE_QUEUE_SIZE; i++) {
		vp = &is->pictq[i];
		if (vp->pFrameRGB) {
			av_free(vp->pFrameRGB);
			vp->pFrameRGB = 0;
		}
		if (vp->buffer) {
			av_free(vp->buffer);
			vp->buffer = 0;
		}
	}
	SDL_DestroyMutex(is->pictq_mutex);
	SDL_DestroyCond(is->pictq_cond);

	if (is->img_convert_ctx)
		sws_freeContext(is->img_convert_ctx);
	av_free(is);
	is = NULL;
}

static void do_exit(VideoState *is) {
	if (is) {
		stream_close(is);
	}
	av_lockmgr_register(NULL);
	avformat_network_deinit();
	if (show_status)
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android", "\n");
	SDL_Quit();
	av_log(NULL, AV_LOG_QUIET, "%s", "");
	exit(0);
}

static void sigterm_handler(int sig) {
	exit(123);
}

static int video_open(VideoState *is, int force_set_video_mode) {
	int flags = SDL_HWSURFACE | SDL_ASYNCBLIT | SDL_HWACCEL;
	int w, h;
	VideoPicture *vp = &is->pictq[is->pictq_rindex];

	if (is_full_screen)
		flags |= SDL_FULLSCREEN;
	else
		flags |= SDL_RESIZABLE;

	if (is_full_screen && fs_screen_width) {
		w = fs_screen_width;
		h = fs_screen_height;
	} else if (!is_full_screen && screen_width) {
		w = screen_width;
		h = screen_height;
	} else if (vp->width) {
		w = vp->width;
		h = vp->height;
	} else {
		w = 640;
		h = 480;
	}

	window = SDL_CreateWindow("MySDL", SDL_WINDOWPOS_CENTERED,
	SDL_WINDOWPOS_CENTERED, w, h, SDL_WINDOW_SHOWN | SDL_WINDOWEVENT_RESIZED);

	if (window == NULL) {
		fprintf(stderr, "SDL: could not set video window - exiting\n");
		exit(1);
	}
	renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_TARGETTEXTURE);

	if (!renderer) {
		fprintf(stderr, "Couldn't set create renderer: %s\n", SDL_GetError());
		exit(1);
	}

	is->width = w;
	is->height = h;
	return 0;
}

/* display the current picture, if any */
static void video_display(VideoState *is) {
	if (window == NULL)
		video_open(is, 0);
	if (is->video_st)
		video_image_display(is);
}

static int refresh_thread(void *opaque) {
	VideoState *is = opaque;
	while (!is->abort_request) {
		SDL_Event event;
		event.type = FF_REFRESH_EVENT;
		event.user.data1 = opaque;
		if (!is->refresh && (!is->paused || is->force_refresh)) {
			is->refresh = 1;
			SDL_PushEvent(&event);
		}

		event.type = KU_PLAYER_BUFCHK;
		event.user.data1 = opaque;
		SDL_PushEvent(&event);

		//FIXME ideally we should wait the correct time but SDLs event passing is so slow it would be silly
		usleep(
				is->audio_st && is->show_mode != SHOW_MODE_VIDEO ?
						rdftspeed * 1000 : 20000);
//		int s  = 		is->audio_st && is->show_mode != SHOW_MODE_VIDEO ?rdftspeed * 1000 : 5000;
//		LOGV("sleep time:%d\n", s);
	}
	return 0;
}

/* seek in the stream */
static void stream_seek(VideoState *is, int64_t pos, int64_t rel,
		int seek_by_bytes) {
	if (!is->seek_req) {
		is->seek_pos = pos;
		is->seek_rel = rel;
		is->seek_flags &= ~AVSEEK_FLAG_BYTE;
		if (seek_by_bytes)
			is->seek_flags |= AVSEEK_FLAG_BYTE;
		is->seek_req = 1;
	}
}

/* pause or resume the video */
static void stream_toggle_pause(VideoState *is) {
	if (is->paused) {
		is->frame_timer += av_gettime() / 1000000.0
				+ is->video_current_pts_drift - is->video_current_pts;
		if (is->read_pause_return != AVERROR(ENOSYS)) {
			is->video_current_pts = is->video_current_pts_drift
					+ av_gettime() / 1000000.0;
		}
		is->video_current_pts_drift = is->video_current_pts
				- av_gettime() / 1000000.0;
	}
	is->paused = !is->paused;
}

static double compute_target_delay(double delay, VideoState *is) {
	double sync_threshold, diff;
	/* update delay to follow master synchronisation source */
	if (((is->av_sync_type == AV_SYNC_AUDIO_MASTER && is->audio_st)
			|| is->av_sync_type == AV_SYNC_EXTERNAL_CLOCK)) {
		/* if video is slave, we try to correct big delays by
		 duplicating or deleting a frame */
		diff = get_video_clock(is) - get_master_clock(is);

		/* skip or repeat frame. We take into account the
		 delay to compute the threshold. I still don't know
		 if it is the best guess */
		sync_threshold = FFMAX(AV_SYNC_THRESHOLD, delay);
		if (fabs(diff) < AV_NOSYNC_THRESHOLD) {
			if (diff <= -sync_threshold)
				delay = 0;
			else if (diff >= sync_threshold)
				delay = 2 * delay;
		}
	}

#ifdef DEBUG
	av_dlog(NULL, "video: delay=%0.3f A-V=%f\n", delay, -diff);
	__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android", "video: delay=%0.3f A-V=%f\n", delay, -diff);
#endif

	return delay;
}

static void pictq_next_picture(VideoState *is) {
	/* update queue size and signal for next picture */
	if (++is->pictq_rindex == VIDEO_PICTURE_QUEUE_SIZE)
		is->pictq_rindex = 0;

	SDL_LockMutex(is->pictq_mutex);
	is->pictq_size--;
	SDL_CondSignal(is->pictq_cond);
	SDL_UnlockMutex(is->pictq_mutex);
}

static void update_video_pts(VideoState *is, double pts, int64_t pos) {
	double time = av_gettime() / 1000000.0;
	/* update current video pts */
	is->video_current_pts = pts;
	is->video_current_pts_drift = is->video_current_pts - time;
	is->video_current_pos = pos;
	is->frame_last_pts = pts;
}

void calculate_duration(AVFormatContext *ic) {

	if (ic && (ic->duration != AV_NOPTS_VALUE))
		g_total_duration = ic->duration / 1000; //msec
	else
		g_total_duration = 0;
}

/* called to display each frame */
static void video_refresh(void *opaque) {
	VideoState *is = opaque;
	VideoPicture *vp;
	double time;

	if (is->video_st) {
		retry: if (is->pictq_size == 0) {
			SDL_LockMutex(is->pictq_mutex);
			if (is->frame_last_dropped_pts != AV_NOPTS_VALUE
					&& is->frame_last_dropped_pts > is->frame_last_pts) {
				update_video_pts(is, is->frame_last_dropped_pts,
						is->frame_last_dropped_pos);
				is->frame_last_dropped_pts = AV_NOPTS_VALUE;
			}
			SDL_UnlockMutex(is->pictq_mutex);
			// nothing to do, no picture to display in the que
		} else {
			double last_duration, duration, delay;
			/* dequeue the picture */
			vp = &is->pictq[is->pictq_rindex];

			if (vp->skip) {
				pictq_next_picture(is);
				goto retry;
			}

			if (is->paused)
				goto display;

			/* compute nominal last_duration */
			last_duration = vp->pts - is->frame_last_pts;
			if (last_duration > 0 && last_duration < 10.0) {
				/* if duration of the last frame was sane, update last_duration in video state */
				is->frame_last_duration = last_duration;
			}
			delay = compute_target_delay(is->frame_last_duration, is);

			time = av_gettime() / 1000000.0;
			if (time < is->frame_timer + delay)
				return;

			if (delay > 0)
				is->frame_timer += delay
						* FFMAX(1, floor((time - is->frame_timer) / delay));

			SDL_LockMutex(is->pictq_mutex);
			update_video_pts(is, vp->pts, vp->pos);
			SDL_UnlockMutex(is->pictq_mutex);

			if (is->pictq_size > 1) {
				VideoPicture *nextvp = &is->pictq[(is->pictq_rindex + 1)
						% VIDEO_PICTURE_QUEUE_SIZE];
				duration = nextvp->pts - vp->pts; // More accurate this way, 1/time_base is often not reflecting FPS
			} else {
				duration = vp->duration;
			}

			if ((framedrop > 0 || (framedrop && is->audio_st))
					&& time > is->frame_timer + duration) {
				if (is->pictq_size > 1) {
					is->frame_drops_late++;
					pictq_next_picture(is);
					goto retry;
				}
			}

			display:
			/* display picture */
			if (!display_disable)
				video_display(is);

			if (!is->paused)
				pictq_next_picture(is);
		}

	}
//    else if (is->audio_st) {
//        /* draw the next audio frame */
//
//        /* if only audio stream, then display the audio bars (better
//           than nothing, just to test the implementation */
//
//        /* display picture */
//        if (!display_disable)
//            video_display(is);
//    }
	is->force_refresh = 0;
	if (show_status) {
		static int64_t last_time;
		int64_t cur_time;
		int aqsize, vqsize, sqsize;
		double av_diff;

		cur_time = av_gettime();
		if (!last_time || (cur_time - last_time) >= 30000) {
			aqsize = 0;
			vqsize = 0;
			sqsize = 0;
			if (is->audio_st)
				aqsize = is->audioq.size;
			if (is->video_st)
				vqsize = is->videoq.size;
			av_diff = 0;
			if (is->audio_st && is->video_st)
				av_diff = get_audio_clock(is) - get_video_clock(is);
#ifdef DEBUG
			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
					"%7.2f A-V:%7.3f fd=%4d aq=%5dKB vq=%5dKB sq=%5dB f=%"PRId64"/%"PRId64"   \r", get_master_clock(is), av_diff, is->frame_drops_early + is->frame_drops_late, aqsize / 1024, vqsize / 1024, sqsize, is->video_st ? is->video_st->codec->pts_correction_num_faulty_dts : 0, is->video_st ? is->video_st->codec->pts_correction_num_faulty_pts : 0);
			fflush(stdout);
#endif
			last_time = cur_time;
		}
	}
}

/* allocate a picture (needs to do that in main thread to avoid
 potential locking problems */
static void alloc_picture(AllocEventProps *event_props) {
	VideoState *is = event_props->is;
	AVFrame *frame = event_props->frame;
	VideoPicture *vp;

	vp = &is->pictq[is->pictq_windex];

	if (vp->pFrameRGB) {
		// we already have one make another, bigger/smaller
		if (vp->pFrameRGB) {
			av_free(vp->pFrameRGB);
			vp->pFrameRGB = 0;
		}
		if (vp->buffer) {
			av_free(vp->buffer);
			vp->buffer = 0;
		}
	}
	vp->width = frame->width;
	vp->height = frame->height;
	vp->pix_fmt = frame->format;

	//video_open(event_props->is, 0);

	vp->pFrameRGB = avcodec_alloc_frame();

	vp->width = is->video_st->codec->width;
	vp->height = is->video_st->codec->height;

	vp->numBytes = avpicture_get_size(dst_fix_fmt, vp->width, vp->height);
	vp->buffer = (uint8_t *) av_malloc(vp->numBytes * sizeof(uint8_t));

	if (!vp->pFrameRGB || !vp->buffer) {
		printf("can not get frame memory, exit\n");
	}

	avpicture_fill((AVPicture*) vp->pFrameRGB, vp->buffer, dst_fix_fmt,
			vp->width, vp->height);

	SDL_LockMutex(is->pictq_mutex);
	vp->allocated = 1;
	SDL_CondSignal(is->pictq_cond);
	SDL_UnlockMutex(is->pictq_mutex);
}

static int queue_picture(VideoState *is, AVFrame *src_frame, double pts1,
		int64_t pos) {
	VideoPicture *vp;
	double frame_delay, pts = pts1;

	/* compute the exact PTS for the picture if it is omitted in the stream
	 * pts1 is the dts of the pkt / pts of the frame */
	if (pts != 0) {
		/* update video clock with pts, if present */
		is->video_clock = pts;
	} else {
		pts = is->video_clock;
	}
	/* update video clock for next frame */
	frame_delay = av_q2d(is->video_st->codec->time_base);
	/* for MPEG2, the frame can be repeated, so we update the
	 clock accordingly */
	frame_delay += src_frame->repeat_pict * (frame_delay * 0.5);
	is->video_clock += frame_delay;

	/* wait until we have space to put a new picture */
	SDL_LockMutex(is->pictq_mutex);

	while (is->pictq_size >= VIDEO_PICTURE_QUEUE_SIZE
			&& !is->videoq.abort_request) {
		SDL_CondWait(is->pictq_cond, is->pictq_mutex);
	}

	SDL_UnlockMutex(is->pictq_mutex);

	if (is->videoq.abort_request)
		return -1;

	vp = &is->pictq[is->pictq_windex];

	vp->duration = frame_delay;

	/* alloc or resize hardware picture buffer */
	if (!vp->pFrameRGB || vp->reallocate || vp->width != src_frame->width
			|| vp->height != src_frame->height) {
		SDL_Event event;
		AllocEventProps event_props;

		event_props.frame = src_frame;
		event_props.is = is;

		vp->allocated = 0;
		vp->reallocate = 0;

		/* the allocation must be done in the main thread to avoid
		 locking problems. We wait in this block for the event to complete,
		 so we can pass a pointer to event_props to it. */
		event.type = FF_ALLOC_EVENT;
		event.user.data1 = &event_props;
		SDL_PushEvent(&event);

		/* wait until the picture is allocated */
		SDL_LockMutex(is->pictq_mutex);
		while (!vp->allocated && !is->videoq.abort_request) {
			SDL_CondWait(is->pictq_cond, is->pictq_mutex);
		}

		/* if the queue is aborted, we have to pop the pending ALLOC event or wait for the allocation to complete */
		//if (is->videoq.abort_request && SDL_PeepEvents(&event, 1, SDL_GETEVENT, SDL_EVENTMASK(FF_ALLOC_EVENT)) != 1) {
		if (is->videoq.abort_request) {
			while (!vp->allocated) {
				SDL_CondWait(is->pictq_cond, is->pictq_mutex);
			}
		}
		SDL_UnlockMutex(is->pictq_mutex);

		if (is->videoq.abort_request)
			return -1;
	}

	/* if the frame is not skipped, then display it */
	if (vp->pFrameRGB) {

		if (is->img_convert_ctx == NULL) {
			is->img_convert_ctx = sws_getCachedContext(is->img_convert_ctx,
					vp->width, vp->height, vp->pix_fmt, vp->width, vp->height,
					dst_fix_fmt, sws_flags, NULL, NULL, NULL);
			if (is->img_convert_ctx == NULL) {
				__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
						"Cannot initialize the conversion context!\n");
				exit(1);
			}
		}
		sws_scale(is->img_convert_ctx, src_frame->data, src_frame->linesize, 0,
				is->video_st->codec->height, vp->pFrameRGB->data,
				vp->pFrameRGB->linesize);
		vp->sample_aspect_ratio = av_guess_sample_aspect_ratio(is->ic,
				is->video_st, src_frame);

		vp->pts = pts;
		vp->pos = pos;
		vp->skip = 0;

		/* now we can update the picture count */
		if (++is->pictq_windex == VIDEO_PICTURE_QUEUE_SIZE)
			is->pictq_windex = 0;
		SDL_LockMutex(is->pictq_mutex);
		is->pictq_size++;
		SDL_UnlockMutex(is->pictq_mutex);
	}

	return 0;
}

static int get_video_frame(VideoState *is, AVFrame *frame, int64_t *pts,
		AVPacket *pkt) {
	int got_picture, i;

	if (packet_queue_get(&is->videoq, pkt, 1) < 0)
		return -1;

	if (pkt->data == flush_pkt.data) {
		avcodec_flush_buffers(is->video_st->codec);

		SDL_LockMutex(is->pictq_mutex);
		// Make sure there are no long delay timers (ideally we should just flush the que but thats harder)
		for (i = 0; i < VIDEO_PICTURE_QUEUE_SIZE; i++) {
			is->pictq[i].skip = 1;
		}
		while (is->pictq_size && !is->videoq.abort_request) {
			SDL_CondWait(is->pictq_cond, is->pictq_mutex);
		}
		is->video_current_pos = -1;
		is->frame_last_pts = AV_NOPTS_VALUE;
		is->frame_last_duration = 0;
		is->frame_timer = (double) av_gettime() / 1000000.0;
		is->frame_last_dropped_pts = AV_NOPTS_VALUE;
		SDL_UnlockMutex(is->pictq_mutex);

		return 0;
	}

	//解码跳帧算法
	double delay = get_master_clock(is) - get_video_clock(is);
	if (delay > SKIP_DELAY_TIME && g_skip) {
		is->video_st->codec->skip_frame = AVDISCARD_BIDIR;
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"Skip delay :%.3f, video: %.3f, mask clock:%.3f\n", delay,
				get_video_clock(is), get_master_clock(is));
	} else
		is->video_st->codec->skip_frame = AVDISCARD_NONE;

	avcodec_decode_video2(is->video_st->codec, frame, &got_picture, pkt);
//	__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
//													"avcodec_decode_video2成功\n");

//	usleep(1);
	if (got_picture) {
		int ret = 1;

		if (decoder_reorder_pts == -1) {
			*pts = av_frame_get_best_effort_timestamp(frame);
		} else if (decoder_reorder_pts) {
			*pts = frame->pkt_pts;
		} else {
			*pts = frame->pkt_dts;
		}

		if (*pts == AV_NOPTS_VALUE) {
			*pts = 0;
		}

//		if (((is->av_sync_type == AV_SYNC_AUDIO_MASTER && is->audio_st)
//				|| is->av_sync_type == AV_SYNC_EXTERNAL_CLOCK)
//				&& (framedrop > 0 || (framedrop && is->audio_st))) {
//			SDL_LockMutex(is->pictq_mutex);
//			if (is->frame_last_pts != AV_NOPTS_VALUE && *pts) {
//				double clockdiff = get_video_clock(is) - get_master_clock(is);
//				double dpts = av_q2d(is->video_st->time_base) * *pts;
//				double ptsdiff = dpts - is->frame_last_pts;
//				//LOGV("clockdiff:%f, ptsdiff: %f\n", clockdiff, ptsdiff);
//				if (fabs(clockdiff) < AV_NOSYNC_THRESHOLD && ptsdiff > 0
//						&& ptsdiff < AV_NOSYNC_THRESHOLD
//						&& clockdiff + ptsdiff - is->frame_last_filter_delay
//								< 0) {
//					is->frame_last_dropped_pos = pkt->pos;
//					is->frame_last_dropped_pts = dpts;
//					is->frame_drops_early++;
//					ret = 0;
//				}
//			}
//			SDL_UnlockMutex(is->pictq_mutex);
//		}

		if (ret)
			is->frame_last_returned_time = av_gettime() / 1000000.0;

		return ret;
	}
	return 0;
}

static int video_thread(void *arg) {
	VideoState *is = arg;
	AVFrame *frame = avcodec_alloc_frame();
	int64_t pts_int = AV_NOPTS_VALUE, pos = -1;
	double pts;
	int ret;

	for (;;) {
		AVPacket pkt;

		while (is->paused && !is->videoq.abort_request)
			SDL_Delay(10);

		ret = get_video_frame(is, frame, &pts_int, &pkt);
		pos = pkt.pos;
		av_free_packet(&pkt);
		if (ret == 0)
			continue;

		if (ret < 0)
			goto the_end;

		is->frame_last_filter_delay = av_gettime() / 1000000.0
				- is->frame_last_returned_time;
		if (fabs(is->frame_last_filter_delay) > AV_NOSYNC_THRESHOLD / 10.0)
			is->frame_last_filter_delay = 0;

		pts = pts_int * av_q2d(is->video_st->time_base);

#ifdef DEBUG
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android", "video_st time_base: %f, pts_int:%d\n",
				av_q2d(is->video_st->time_base), pts_int);
#endif

		ret = queue_picture(is, frame, pts, pos);
		if (ret < 0)
			goto the_end;

		if (is->step)
			stream_toggle_pause(is);
	}
	the_end: avcodec_flush_buffers(is->video_st->codec);
	av_free(frame);
	return 0;
}

/* copy samples for viewing in editor window */
static void update_sample_display(VideoState *is, short *samples,
		int samples_size) {
	int size, len;

	size = samples_size / sizeof(short);
	while (size > 0) {
		len = SAMPLE_ARRAY_SIZE - is->sample_array_index;
		if (len > size)
			len = size;
		memcpy(is->sample_array + is->sample_array_index, samples,
				len * sizeof(short));
		samples += len;
		is->sample_array_index += len;
		if (is->sample_array_index >= SAMPLE_ARRAY_SIZE)
			is->sample_array_index = 0;
		size -= len;
	}
}

/* return the wanted number of samples to get better sync if sync_type is video
 * or external master clock */
static int synchronize_audio(VideoState *is, int nb_samples) {
	int wanted_nb_samples = nb_samples;

	/* if not master, then we try to remove or add samples to correct the clock */
	if (((is->av_sync_type == AV_SYNC_VIDEO_MASTER && is->video_st)
			|| is->av_sync_type == AV_SYNC_EXTERNAL_CLOCK)) {
		double diff, avg_diff;
		int min_nb_samples, max_nb_samples;

		diff = get_audio_clock(is) - get_master_clock(is);

		if (diff < AV_NOSYNC_THRESHOLD) {
			is->audio_diff_cum = diff
					+ is->audio_diff_avg_coef * is->audio_diff_cum;
			if (is->audio_diff_avg_count < AUDIO_DIFF_AVG_NB) {
				/* not enough measures to have a correct estimate */
				is->audio_diff_avg_count++;
			} else {
				/* estimate the A-V difference */
				avg_diff = is->audio_diff_cum * (1.0 - is->audio_diff_avg_coef);

				if (fabs(avg_diff) >= is->audio_diff_threshold) {
					wanted_nb_samples = nb_samples
							+ (int) (diff * is->audio_src_freq);
					min_nb_samples = ((nb_samples
							* (100 - SAMPLE_CORRECTION_PERCENT_MAX) / 100));
					max_nb_samples = ((nb_samples
							* (100 + SAMPLE_CORRECTION_PERCENT_MAX) / 100));
					wanted_nb_samples = FFMIN(
							FFMAX(wanted_nb_samples, min_nb_samples),
							max_nb_samples);
				}
				av_dlog(NULL,
						"diff=%f adiff=%f sample_diff=%d apts=%0.3f vpts=%0.3f %f\n",
						diff, avg_diff, wanted_nb_samples - nb_samples,
						is->audio_clock, is->video_clock,
						is->audio_diff_threshold);
			}
		} else {
			/* too big difference : may be initial PTS errors, so
			 reset A-V filter */
			is->audio_diff_avg_count = 0;
			is->audio_diff_cum = 0;
		}
	}

	return wanted_nb_samples;
}

/* decode one audio frame and returns its uncompressed size */
static int audio_decode_frame(VideoState *is, double *pts_ptr) {
	AVPacket *pkt_temp = &is->audio_pkt_temp;
	AVPacket *pkt = &is->audio_pkt;
	AVCodecContext *dec = is->audio_st->codec;
	int len1, len2, data_size, resampled_data_size;
	int64_t dec_channel_layout;
	int got_frame;
	double pts;
	int new_packet = 0;
	int flush_complete = 0;
	int wanted_nb_samples;

	for (;;) {
		/* NOTE: the audio packet can contain several frames */
		while (pkt_temp->size > 0 || (!pkt_temp->data && new_packet)) {
			if (!is->frame) {
				if (!(is->frame = avcodec_alloc_frame()))
					return AVERROR(ENOMEM);
			} else
				avcodec_get_frame_defaults(is->frame);

			if (flush_complete)
				break;
			new_packet = 0;
			len1 = avcodec_decode_audio4(dec, is->frame, &got_frame, pkt_temp);
			if (len1 < 0) {
				/* if error, we skip the frame */
				pkt_temp->size = 0;
				break;
			}

			pkt_temp->data += len1;
			pkt_temp->size -= len1;

			if (!got_frame) {
				/* stop sending empty packets if the decoder is finished */
				if (!pkt_temp->data
						&& (dec->codec->capabilities & CODEC_CAP_DELAY))
					flush_complete = 1;
				continue;
			}
			data_size = av_samples_get_buffer_size(NULL, dec->channels,
					is->frame->nb_samples, dec->sample_fmt, 1);

			dec_channel_layout =
					(dec->channel_layout
							&& dec->channels
									== av_get_channel_layout_nb_channels(
											dec->channel_layout)) ?
							dec->channel_layout :
							av_get_default_channel_layout(dec->channels);
			wanted_nb_samples = synchronize_audio(is, is->frame->nb_samples);

			if (dec->sample_fmt != is->audio_src_fmt
					|| dec_channel_layout != is->audio_src_channel_layout
					|| dec->sample_rate != is->audio_src_freq
					|| (wanted_nb_samples != is->frame->nb_samples
							&& !is->swr_ctx)) {
				if (is->swr_ctx)
					swr_free(&is->swr_ctx);
				is->swr_ctx = swr_alloc_set_opts(NULL,
						is->audio_tgt_channel_layout, is->audio_tgt_fmt,
						is->audio_tgt_freq, dec_channel_layout, dec->sample_fmt,
						dec->sample_rate, 0, NULL);
				if (!is->swr_ctx || swr_init(is->swr_ctx) < 0) {
					__android_log_print(ANDROID_LOG_VERBOSE,
							"anvizplayer_android",
							"Cannot create sample rate converter for conversion of %d Hz %s %d channels to %d Hz %s %d channels!\n",
							dec->sample_rate,
							av_get_sample_fmt_name(dec->sample_fmt),
							dec->channels, is->audio_tgt_freq,
							av_get_sample_fmt_name(is->audio_tgt_fmt),
							is->audio_tgt_channels);
					break;
				}
				is->audio_src_channel_layout = dec_channel_layout;
				is->audio_src_channels = dec->channels;
				is->audio_src_freq = dec->sample_rate;
				is->audio_src_fmt = dec->sample_fmt;
			}

			resampled_data_size = data_size;
			if (is->swr_ctx) {
				const uint8_t *in[] = { is->frame->data[0] };
				uint8_t *out[] = { is->audio_buf2 };
				if (wanted_nb_samples != is->frame->nb_samples) {
					if (swr_set_compensation(is->swr_ctx,
							(wanted_nb_samples - is->frame->nb_samples)
									* is->audio_tgt_freq / dec->sample_rate,
							wanted_nb_samples * is->audio_tgt_freq
									/ dec->sample_rate) < 0) {
						__android_log_print(ANDROID_LOG_VERBOSE,
								"anvizplayer_android",
								"swr_set_compensation() failed\n");
						break;
					}
				}
				len2 = swr_convert(is->swr_ctx, out,
						sizeof(is->audio_buf2) / is->audio_tgt_channels
								/ av_get_bytes_per_sample(is->audio_tgt_fmt),
						in, is->frame->nb_samples);
				if (len2 < 0) {
					__android_log_print(ANDROID_LOG_VERBOSE,
							"anvizplayer_android", "audio_resample() failed\n");
					break;
				}
				if (len2
						== sizeof(is->audio_buf2) / is->audio_tgt_channels
								/ av_get_bytes_per_sample(is->audio_tgt_fmt)) {
					__android_log_print(ANDROID_LOG_VERBOSE,
							"anvizplayer_android",
							"warning: audio buffer is probably too small\n");
					swr_init(is->swr_ctx);
				}
				is->audio_buf = is->audio_buf2;
				resampled_data_size = len2 * is->audio_tgt_channels
						* av_get_bytes_per_sample(is->audio_tgt_fmt);
			} else {
				is->audio_buf = is->frame->data[0];
			}

			/* if no pts, then compute it */
			pts = is->audio_clock;
			*pts_ptr = pts;
			is->audio_clock += (double) data_size
					/ (dec->channels * dec->sample_rate
							* av_get_bytes_per_sample(dec->sample_fmt));
#ifdef DEBUG
			{
				static double last_clock;
				__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android", "audio: delay=%0.3f clock=%0.3f pts=%0.3f\n",
						is->audio_clock - last_clock,
						is->audio_clock, pts);
				last_clock = is->audio_clock;
			}
#endif
			return resampled_data_size;
		}

		/* free the current packet */
		if (pkt->data)
			av_free_packet(pkt);
		memset(pkt_temp, 0, sizeof(*pkt_temp));

		if (is->paused || is->audioq.abort_request) {
			return -1;
		}

		/* read next packet */
		if ((new_packet = packet_queue_get(&is->audioq, pkt, 1)) < 0)
			return -1;

		if (pkt->data == flush_pkt.data) {
			avcodec_flush_buffers(dec);
			flush_complete = 0;
		}

		*pkt_temp = *pkt;

		/* if update the audio clock with the pts */
		if (pkt->pts != AV_NOPTS_VALUE) {
			is->audio_clock = av_q2d(is->audio_st->time_base) * pkt->pts;
		}
	}
}

/* prepare a new audio buffer */
static void sdl_audio_callback(void *opaque, Uint8 *stream, int len) {
	VideoState *is = opaque;
	int audio_size, len1;
	int bytes_per_sec;
	int frame_size = av_samples_get_buffer_size(NULL, is->audio_tgt_channels, 1,
			is->audio_tgt_fmt, 1);
	double pts;

	audio_callback_time = av_gettime();

	while (len > 0) {
		if (is->audio_buf_index >= is->audio_buf_size) {
			audio_size = audio_decode_frame(is, &pts);
			if (audio_size < 0) {
				/* if error, just output silence */
				is->audio_buf = is->silence_buf;
				is->audio_buf_size = sizeof(is->silence_buf) / frame_size
						* frame_size;
			} else {
				if (is->show_mode != SHOW_MODE_VIDEO)
					update_sample_display(is, (int16_t *) is->audio_buf,
							audio_size);
				is->audio_buf_size = audio_size;
			}
			is->audio_buf_index = 0;
		}
		len1 = is->audio_buf_size - is->audio_buf_index;
		if (len1 > len)
			len1 = len;
		memcpy(stream, (uint8_t *) is->audio_buf + is->audio_buf_index, len1);
		len -= len1;
		stream += len1;
		is->audio_buf_index += len1;
	}
	bytes_per_sec = is->audio_tgt_freq * is->audio_tgt_channels
			* av_get_bytes_per_sample(is->audio_tgt_fmt);
	is->audio_write_buf_size = is->audio_buf_size - is->audio_buf_index;
	/* Let's assume the audio driver that is used by SDL has two periods. */
	is->audio_current_pts = is->audio_clock
			- (double) (2 * is->audio_hw_buf_size + is->audio_write_buf_size)
					/ bytes_per_sec;
	is->audio_current_pts_drift = is->audio_current_pts
			- audio_callback_time / 1000000.0;
}

/* open a given stream. Return 0 if OK */
static int stream_component_open(VideoState *is, int stream_index) {
	AVFormatContext *ic = is->ic;
	AVCodecContext *avctx;
	AVCodec *codec;
	SDL_AudioSpec wanted_spec, spec;
	AVDictionary *opts = NULL;
	AVDictionaryEntry *t = NULL;
	int64_t wanted_channel_layout = 0;
	int wanted_nb_channels;
	const char *env;

	if (stream_index < 0 || stream_index >= ic->nb_streams)
		return -1;
	avctx = ic->streams[stream_index]->codec;

	codec = avcodec_find_decoder(avctx->codec_id);

	switch (avctx->codec_type) {
	case AVMEDIA_TYPE_AUDIO:
		is->last_audio_stream = stream_index;
		if (audio_codec_name)
			codec = avcodec_find_decoder_by_name(audio_codec_name);
		break;
	case AVMEDIA_TYPE_SUBTITLE:
		is->last_subtitle_stream = stream_index;
		if (subtitle_codec_name)
			codec = avcodec_find_decoder_by_name(subtitle_codec_name);
		break;
	case AVMEDIA_TYPE_VIDEO:
		is->last_video_stream = stream_index;
		if (video_codec_name)
			codec = avcodec_find_decoder_by_name(video_codec_name);
		break;
	default:
		break;
	}
	if (!codec)
		return -1;

	avctx->workaround_bugs = workaround_bugs;
	avctx->lowres = lowres;
	if (avctx->lowres > codec->max_lowres) {
		av_log(avctx, AV_LOG_WARNING,
				"The maximum value for lowres supported by the decoder is %d\n",
				codec->max_lowres);
		avctx->lowres = codec->max_lowres;
	}
	avctx->idct_algo = idct;
	avctx->skip_frame = skip_frame;
	avctx->skip_idct = skip_idct;
	avctx->skip_loop_filter = skip_loop_filter;
	avctx->error_concealment = error_concealment;

	if (avctx->lowres)
		avctx->flags |= CODEC_FLAG_EMU_EDGE;
	//if (fast)
	avctx->flags2 |= CODEC_FLAG2_FAST;
	if (codec->capabilities & CODEC_CAP_DR1)
		avctx->flags |= CODEC_FLAG_EMU_EDGE;

	if (avctx->codec_type == AVMEDIA_TYPE_AUDIO) {
		memset(&is->audio_pkt_temp, 0, sizeof(is->audio_pkt_temp));
		env = SDL_getenv("SDL_AUDIO_CHANNELS");
		if (env)
			wanted_channel_layout = av_get_default_channel_layout(
					SDL_atoi(env));
		if (!wanted_channel_layout) {
			wanted_channel_layout =
					(avctx->channel_layout
							&& avctx->channels
									== av_get_channel_layout_nb_channels(
											avctx->channel_layout)) ?
							avctx->channel_layout :
							av_get_default_channel_layout(avctx->channels);
			wanted_channel_layout &= ~AV_CH_LAYOUT_STEREO_DOWNMIX;
			wanted_nb_channels = av_get_channel_layout_nb_channels(
					wanted_channel_layout);
			/* SDL only supports 1, 2, 4 or 6 channels at the moment, so we have to make sure not to request anything else. */
			while (wanted_nb_channels > 0
					&& (wanted_nb_channels == 3 || wanted_nb_channels == 5
							|| wanted_nb_channels > 6)) {
				wanted_nb_channels--;
				wanted_channel_layout = av_get_default_channel_layout(
						wanted_nb_channels);
			}
		}
		wanted_spec.channels = av_get_channel_layout_nb_channels(
				wanted_channel_layout);
		wanted_spec.freq = avctx->sample_rate;

		if (wanted_spec.freq <= 0 || wanted_spec.channels <= 0) {
			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
					"Invalid sample rate or channel count!\n");
			return -1;
		}
	}

	if (!av_dict_get(opts, "threads", NULL, 0))
		av_dict_set(&opts, "threads", "auto", 0);
	if (!codec || avcodec_open2(avctx, codec, &opts) < 0)
		return -1;
	if ((t = av_dict_get(opts, "", NULL, AV_DICT_IGNORE_SUFFIX))) {
		av_log(NULL, AV_LOG_ERROR, "Option %s not found.\n", t->key);
		return AVERROR_OPTION_NOT_FOUND;
	}

	/* prepare audio output */
	if (avctx->codec_type == AVMEDIA_TYPE_AUDIO) {
		wanted_spec.format = AUDIO_S16SYS;
		wanted_spec.silence = 0;
		wanted_spec.samples = SDL_AUDIO_BUFFER_SIZE;
		wanted_spec.callback = sdl_audio_callback;
		wanted_spec.userdata = is;
		if (SDL_OpenAudio(&wanted_spec, &spec) < 0) {
			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
					"SDL_OpenAudio: %s\n", SDL_GetError());
			//return -1;
		}
		is->audio_hw_buf_size = spec.size;
		if (spec.format != AUDIO_S16SYS) {
			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
					"SDL advised audio format %d is not supported!\n",
					spec.format);
			return -1;
		}
		if (spec.channels != wanted_spec.channels) {
			wanted_channel_layout = av_get_default_channel_layout(
					spec.channels);
			if (!wanted_channel_layout) {
				__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
						"SDL advised channel count %d is not supported!\n",
						spec.channels);
				return -1;
			}
		}
		is->audio_src_fmt = is->audio_tgt_fmt = AV_SAMPLE_FMT_S16;
		is->audio_src_freq = is->audio_tgt_freq = spec.freq;
		is->audio_src_channel_layout = is->audio_tgt_channel_layout =
				wanted_channel_layout;
		is->audio_src_channels = is->audio_tgt_channels = spec.channels;
	}

	ic->streams[stream_index]->discard = AVDISCARD_DEFAULT;
	switch (avctx->codec_type) {
	case AVMEDIA_TYPE_AUDIO:
		is->audio_stream = stream_index;
		is->audio_st = ic->streams[stream_index];
		is->audio_buf_size = 0;
		is->audio_buf_index = 0;

		/* init averaging filter */
		is->audio_diff_avg_coef = exp(log(0.01) / AUDIO_DIFF_AVG_NB);
		is->audio_diff_avg_count = 0;
		/* since we do not have a precise anough audio fifo fullness,
		 we correct audio sync only if larger than this threshold */
		is->audio_diff_threshold = 2.0 * SDL_AUDIO_BUFFER_SIZE
				/ wanted_spec.freq;

		memset(&is->audio_pkt, 0, sizeof(is->audio_pkt));
		packet_queue_start(&is->audioq);
		SDL_PauseAudio(0);
		break;
	case AVMEDIA_TYPE_VIDEO:
		is->video_stream = stream_index;
		is->video_st = ic->streams[stream_index];

		packet_queue_start(&is->videoq);
		is->video_tid = SDL_CreateThread(video_thread, "video_thread", is);
		break;
	default:
		break;
	}
	return 0;
}

static void stream_component_close(VideoState *is, int stream_index) {
	AVFormatContext *ic = is->ic;
	AVCodecContext *avctx;

	if (stream_index < 0 || stream_index >= ic->nb_streams)
		return;
	avctx = ic->streams[stream_index]->codec;
	switch (avctx->codec_type) {
	case AVMEDIA_TYPE_AUDIO:
		packet_queue_abort(&is->audioq);

		packet_queue_flush(&is->audioq);
		av_free_packet(&is->audio_pkt);
		if (is->swr_ctx)
			swr_free(&is->swr_ctx);
		av_freep(&is->audio_buf1);
		is->audio_buf = NULL;
		av_freep(&is->frame);

		if (is->rdft) {
			av_rdft_end(is->rdft);
			av_freep(&is->rdft_data);
			is->rdft = NULL;
			is->rdft_bits = 0;
		}

		SDL_CloseAudio();
		break;
	case AVMEDIA_TYPE_VIDEO:
		packet_queue_abort(&is->videoq);

		/* note: we also signal this mutex to make sure we deblock the
		 video thread in all cases */
		SDL_LockMutex(is->pictq_mutex);
		SDL_CondSignal(is->pictq_cond);
		SDL_UnlockMutex(is->pictq_mutex);

		SDL_WaitThread(is->video_tid, NULL);

		packet_queue_flush(&is->videoq);
		break;
	default:
		break;
	}

	ic->streams[stream_index]->discard = AVDISCARD_ALL;
	avcodec_close(avctx);
	switch (avctx->codec_type) {
	case AVMEDIA_TYPE_AUDIO:
		is->audio_st = NULL;
		is->audio_stream = -1;
		break;
	case AVMEDIA_TYPE_VIDEO:
		is->video_st = NULL;
		is->video_stream = -1;
		break;
	default:
		break;
	}
}

static int decode_interrupt_cb(void *ctx) {
	VideoState *is = ctx;
	return is->abort_request;
}

#define BUF_SIZE (1024*300)
extern int iflag;
extern char iBuff[BUF_SIZE];
extern int iSize;
extern int gm_index;
char data[BUF_SIZE];
char retdata[BUF_SIZE * 4];
int read_data(void *opaque, uint8_t *buf, int buf_size) {
	static int iCnt = 0;

	static int iretSize = 0;
	static int iDataSize = 0;

	extern int getReceiveVideo(char *buf, int *iSize);

	static int fflag = 0;
	static int delta = 0;
	__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
			"回调函数=========%d\n", buf_size);
	//	已读数组游标
	static int buf_size_2 = 0;

	while (1) {
		if (0 == fflag) {
			while (1) {
				if (-1 == gm_index) {
					usleep(100);
					continue;
				}

				if (getReceiveVideo(retdata, &iretSize)) {
					break;
				}
				usleep(10000);
			}
//			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
//					"iretSize = %d \r\n ",iretSize);
		}

		if (iretSize > buf_size + buf_size_2) {
			memset(buf, 0, buf_size);
			memcpy(buf, (char *) (&(retdata[buf_size_2])), buf_size);
			delta = 0;

			fflag = 1;
			buf_size_2 += buf_size;
//			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android","buf_size = %d\r\n",buf_size);

			return buf_size;
		} else {
			delta = iretSize - buf_size_2;
//			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android","delta = %d\r\n",delta);
			memcpy(buf, (char *) (&(retdata[buf_size_2])), delta);
			buf_size_2 = 0;
			fflag = 0;
			return delta;
		}
	}
}
/* this thread gets the stream from the disk or the network */
static int read_thread(void *arg) {

	VideoState *is = arg;
	AVFormatContext *ic = NULL;
	int err, i, ret;
	int st_index[AVMEDIA_TYPE_NB];
	AVPacket pkt1, *pkt = &pkt1;
	int eof = 0;
	int pkt_in_play_range = 0;
	memset(st_index, -1, sizeof(st_index));
	is->last_video_stream = is->video_stream = -1;
	is->last_audio_stream = is->audio_stream = -1;

	ic = avformat_alloc_context();
	if (strlen(is->filename) == 0) {
		while (gm_index < 0) {
			usleep(100);
		}
		unsigned char *buf = (unsigned char *) av_mallocz(
				sizeof(uint8_t) * (1024 * 300));

		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"申请一个AVIOContext\n");
		//step1:申请一个AVIOContext
		AVIOContext *avio = avio_alloc_context(buf, 1024 * 300, 0, NULL,
				read_data, NULL, NULL);
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"申请一个AVIOContext成功\n");
		//step2:探测流格式
		if (av_probe_input_buffer(avio, &(is->iformat), "", NULL, 0, 0) < 0) {
			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
					"探测流格式失败\n");
			return -1;
		}
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"探测流格式成功\n");

		ic->pb = avio; //step3:这一步很关键

		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"打开流\n");
	}

	err = avformat_open_input(&ic, is->filename, NULL, NULL);
	if (err < 0) {
		ret = RET_FILE_OPENERROR;
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"打开流失败\n");
		goto fail;
	}
	__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android", "打开流成功\n");

	is->ic = ic;

	if (genpts)
		ic->flags |= AVFMT_FLAG_GENPTS;

	//opts = setup_find_stream_info_opts(ic, codec_opts);

	err = avformat_find_stream_info(ic, NULL);
	if (err < 0) {
		fprintf(stderr, "%s: could not find codec parameters\n", is->filename);
		ret = RET_FORMAT_NOT_SUPPORT;
		goto fail;
	}
	//for (i = 0; i < orig_nb_streams; i++)
	//    av_dict_free(&opts[i]);
	//av_freep(&opts);

	if (ic->pb)
		ic->pb->eof_reached = 0; // FIXME hack, ffplay maybe should not use url_feof() to test for the end

	if (seek_by_bytes < 0)
		seek_by_bytes = !!(ic->iformat->flags & AVFMT_TS_DISCONT);

	/* if seeking requested, we execute it */
	if (start_time != AV_NOPTS_VALUE) {
		int64_t timestamp;

		timestamp = start_time;
		/* add the stream start time */
		if (ic->start_time != AV_NOPTS_VALUE)
			timestamp += ic->start_time;
		ret = avformat_seek_file(ic, -1, INT64_MIN, timestamp, INT64_MAX, 0);
		if (ret < 0) {
			fprintf(stderr, "%s: could not seek to position %0.3f\n",
					is->filename, (double) timestamp / AV_TIME_BASE);
		}
	}

	calculate_duration(ic);

	for (i = 0; i < ic->nb_streams; i++)
		ic->streams[i]->discard = AVDISCARD_ALL;
	if (!video_disable)
		st_index[AVMEDIA_TYPE_VIDEO] = av_find_best_stream(ic,
				AVMEDIA_TYPE_VIDEO, wanted_stream[AVMEDIA_TYPE_VIDEO], -1, NULL,
				0);
	if (!audio_disable)
		st_index[AVMEDIA_TYPE_AUDIO] = av_find_best_stream(ic,
				AVMEDIA_TYPE_AUDIO, wanted_stream[AVMEDIA_TYPE_AUDIO],
				st_index[AVMEDIA_TYPE_VIDEO], NULL, 0);
	if (!video_disable)
		st_index[AVMEDIA_TYPE_SUBTITLE] = av_find_best_stream(ic,
				AVMEDIA_TYPE_SUBTITLE, wanted_stream[AVMEDIA_TYPE_SUBTITLE],
				(st_index[AVMEDIA_TYPE_AUDIO] >= 0 ?
						st_index[AVMEDIA_TYPE_AUDIO] :
						st_index[AVMEDIA_TYPE_VIDEO]), NULL, 0);
//	if (show_status) {
//		av_dump_format(ic, 0, is->filename, 0);
//	}

	is->show_mode = show_mode;

	/* open the streams */
	if (st_index[AVMEDIA_TYPE_AUDIO] >= 0) {
		stream_component_open(is, st_index[AVMEDIA_TYPE_AUDIO]);
	}

	ret = -1;
	if (st_index[AVMEDIA_TYPE_VIDEO] >= 0) {
		ret = stream_component_open(is, st_index[AVMEDIA_TYPE_VIDEO]);
	}
	is->refresh_tid = SDL_CreateThread(refresh_thread, "refresh_thread", is);
	if (is->show_mode == SHOW_MODE_NONE)
		is->show_mode = ret >= 0 ? SHOW_MODE_VIDEO : SHOW_MODE_RDFT;

	if (st_index[AVMEDIA_TYPE_SUBTITLE] >= 0) {
		stream_component_open(is, st_index[AVMEDIA_TYPE_SUBTITLE]);
	}

	if (is->video_stream < 0 && is->audio_stream < 0) {
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"%s: could not open codecs\n", is->filename);
		ret = RET_STREAM_OPEN_FAIL;
		goto fail;
	}

	Android_Notify(MSG_OPEN_OK);

	for (;;) {
		if (is->abort_request)
			break;
		if (is->paused != is->last_paused) {
			is->last_paused = is->paused;
		}
#if CONFIG_RTSP_DEMUXER || CONFIG_MMSH_PROTOCOL
		if (is->paused &&
				(!strcmp(ic->iformat->name, "rtsp") ||
						(ic->pb && !strncmp(input_filename, "mmsh:", 5)))) {
			/* wait 10 ms to avoid trying to get another packet */
			/* XXX: horrible */
			SDL_Delay(10);
			continue;
		}
#endif
		if (is->seek_req) {
			int64_t seek_target = is->seek_pos;
			int64_t seek_min =
					is->seek_rel > 0 ?
							seek_target - is->seek_rel + 2 : INT64_MIN;
			int64_t seek_max =
					is->seek_rel < 0 ?
							seek_target - is->seek_rel - 2 : INT64_MAX;
// FIXME the +-2 is due to rounding being not done in the correct direction in generation
//      of the seek_pos/seek_rel variables

			ret = avformat_seek_file(is->ic, -1, seek_min, seek_target,
					seek_max, is->seek_flags);
			if (ret < 0) {
				fprintf(stderr, "%s: error while seeking\n", is->ic->filename);
			} else {
				if (is->audio_stream >= 0) {
					packet_queue_flush(&is->audioq);
					packet_queue_put(&is->audioq, &flush_pkt);
				}
				if (is->video_stream >= 0) {
					packet_queue_flush(&is->videoq);
					packet_queue_put(&is->videoq, &flush_pkt);
				}
			}
			is->seek_req = 0;
			eof = 0;
		}

		/* if the queue are full, no need to read more */
		/*if (is->audioq.size + is->videoq.size > MAX_QUEUE_SIZE
		 || ((is->audioq.nb_packets > MIN_FRAMES || is->audio_stream < 0
		 || is->audioq.abort_request)
		 && (is->videoq.nb_packets > MIN_FRAMES
		 || is->video_stream < 0
		 || is->videoq.abort_request))) */
		if (is->videoq.nb_packets > MAX_FRAMES) {
			/* wait 10 ms */
			SDL_Delay(10);
			continue;
		}
		if (eof) {
			if (is->video_stream >= 0) {
				av_init_packet(pkt);
				pkt->data = NULL;
				pkt->size = 0;
				pkt->stream_index = is->video_stream;
				packet_queue_put(&is->videoq, pkt);
			}
			if (is->audio_stream >= 0
					&& (is->audio_st->codec->codec->capabilities
							& CODEC_CAP_DELAY)) {
				av_init_packet(pkt);
				pkt->data = NULL;
				pkt->size = 0;
				pkt->stream_index = is->audio_stream;
				packet_queue_put(&is->audioq, pkt);
			}
			SDL_Delay(10);
			if (is->audioq.size + is->videoq.size == 0) {
				if (loop != 1 && (!loop || --loop)) {
					stream_seek(is,
							start_time != AV_NOPTS_VALUE ? start_time : 0, 0,
							0);
				} else if (autoexit) {
					ret = AVERROR_EOF;
					goto fail;
				}
			}
			eof = 0;
			continue;
		}

		ret = av_read_frame(ic, pkt);

//		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android", "av_read_frame====[%d]", ret);
		if (ret < 0) {
			if (ret == AVERROR_EOF || url_feof(ic->pb))
				eof = 1;
			if (ic->pb && ic->pb->error)
				break;
			SDL_Delay(10); /* wait for user event */
			continue;
		}
		/* check if packet is in play range specified by user, then queue, otherwise discard */
		pkt_in_play_range = duration == AV_NOPTS_VALUE
				|| (pkt->pts - ic->streams[pkt->stream_index]->start_time)
						* av_q2d(ic->streams[pkt->stream_index]->time_base)
						- (double) (
								start_time != AV_NOPTS_VALUE ? start_time : 0)
								/ 1000000 <= ((double) duration / 1000000);
		if (pkt->stream_index == is->audio_stream && pkt_in_play_range) {
			packet_queue_put(&is->audioq, pkt);
		} else if (pkt->stream_index == is->video_stream && pkt_in_play_range) {
			packet_queue_put(&is->videoq, pkt);
		} else {
			av_free_packet(pkt);
		}
	}
	/* wait until the end */
	while (!is->abort_request) {
		SDL_Delay(10);
	}

	//ret = RET_OK;
	fail:

	/* close each stream */
	if (is->audio_stream >= 0)
		stream_component_close(is, is->audio_stream);
	if (is->video_stream >= 0)
		stream_component_close(is, is->video_stream);
	if (is->ic) {
		avformat_close_input(&is->ic);
	}
	if (ret != RET_OK) {
		SDL_Event event;

		event.type = FF_QUIT_EVENT;
		event.user.data1 = is;
		SDL_PushEvent(&event);
		Android_Notify(MSG_OPEN_ERROR);
	}
	return ret;
}

static int stream_open(const char *filename, AVInputFormat *iformat) {
	VideoState *is;

	__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
			"stream_open开始");
	is = av_mallocz(sizeof(VideoState));
	if (!is)
		return RET_PREPARE_ERROR;
	av_strlcpy(is->filename, filename, sizeof(is->filename));
	is->iformat = iformat;
	is->ytop = 0;
	is->xleft = 0;

	/* start video display */
	is->pictq_mutex = SDL_CreateMutex();
	is->pictq_cond = SDL_CreateCond();

	packet_queue_init(&is->videoq);
	packet_queue_init(&is->audioq);

	is->av_sync_type = av_sync_type;
	is->read_tid = SDL_CreateThread(read_thread, "read_thread", is);
	if (!is->read_tid) {
		av_free(is);
		return RET_PREPARE_ERROR;
	}
	g_stream = is;
	return RET_OK;
}

static void stream_cycle_channel(VideoState *is, int codec_type) {
	AVFormatContext *ic = is->ic;
	int start_index, stream_index;
	int old_index;
	AVStream *st;

	if (codec_type == AVMEDIA_TYPE_VIDEO) {
		start_index = is->last_video_stream;
		old_index = is->video_stream;
	} else if (codec_type == AVMEDIA_TYPE_AUDIO) {
		start_index = is->last_audio_stream;
		old_index = is->audio_stream;
	}
	stream_index = start_index;
	for (;;) {
		if (++stream_index >= is->ic->nb_streams) {
			if (codec_type == AVMEDIA_TYPE_SUBTITLE) {
				stream_index = -1;
				is->last_subtitle_stream = -1;
				goto the_end;
			}
			if (start_index == -1)
				return;
			stream_index = 0;
		}
		if (stream_index == start_index)
			return;
		st = ic->streams[stream_index];
		if (st->codec->codec_type == codec_type) {
			/* check that parameters are OK */
			switch (codec_type) {
			case AVMEDIA_TYPE_AUDIO:
				if (st->codec->sample_rate != 0 && st->codec->channels != 0)
					goto the_end;
				break;
			case AVMEDIA_TYPE_VIDEO:
			case AVMEDIA_TYPE_SUBTITLE:
				goto the_end;
			default:
				break;
			}
		}
	}
	the_end: stream_component_close(is, old_index);
	stream_component_open(is, stream_index);
}

static void toggle_full_screen(VideoState *is) {
	av_unused int i;
	is_full_screen = !is_full_screen;
#if defined(__APPLE__) && SDL_VERSION_ATLEAST(1, 2, 14)
	/* OS X needs to reallocate the SDL overlays */
	for (i = 0; i < VIDEO_PICTURE_QUEUE_SIZE; i++) {
		is->pictq[i].reallocate = 1;
	}
#endif
	video_open(is, 1);
}

static void toggle_pause(VideoState *is) {
	stream_toggle_pause(is);
	is->step = 0;
}

static void step_to_next_frame(VideoState *is) {
	/* if the stream is paused unpause it, then step */
	if (is->paused)
		stream_toggle_pause(is);
	is->step = 1;
}

/* handle an event sent by the GUI */
static void event_loop(VideoState *cur_stream) {
	SDL_Event event;
	double incr, pos, frac;
	//准备加载数据，先暂停
//	load = 0暂停
	cur_stream->load = 1;
	cur_stream->paused = 1;

	for (;;) {

		double x;
		SDL_WaitEvent(&event);
		switch (event.type) {
		case SDL_KEYDOWN:
			if (exit_on_keydown) {
				do_exit(cur_stream);
				break;
			}
			switch (event.key.keysym.sym) {
			case SDLK_ESCAPE:
			case SDLK_q:
				do_exit(cur_stream);
				break;
			case SDLK_f:
				toggle_full_screen(cur_stream);
				cur_stream->force_refresh = 1;
				break;
			case SDLK_p:
			case SDLK_SPACE:
				toggle_pause(cur_stream);
				break;
			case SDLK_s: // S: Step to next frame
				step_to_next_frame(cur_stream);
				break;
			case SDLK_a:
				stream_cycle_channel(cur_stream, AVMEDIA_TYPE_AUDIO);
				break;
			case SDLK_v:
				stream_cycle_channel(cur_stream, AVMEDIA_TYPE_VIDEO);
				break;
			case SDLK_t:
				stream_cycle_channel(cur_stream, AVMEDIA_TYPE_SUBTITLE);
				break;
			case SDLK_PAGEUP:
				incr = 600.0;
				goto do_seek;
			case SDLK_PAGEDOWN:
				incr = -600.0;
				goto do_seek;
			case SDLK_LEFT:
				incr = -10.0;
				goto do_seek;
			case SDLK_RIGHT:
				incr = 10.0;
				goto do_seek;
			case SDLK_UP:
				incr = 60.0;
				goto do_seek;
			case SDLK_DOWN:
				incr = -60.0;
				do_seek: if (seek_by_bytes) {
					if (cur_stream->video_stream >= 0
							&& cur_stream->video_current_pos >= 0) {
						pos = cur_stream->video_current_pos;
					} else if (cur_stream->audio_stream >= 0
							&& cur_stream->audio_pkt.pos >= 0) {
						pos = cur_stream->audio_pkt.pos;
					} else
						pos = avio_tell(cur_stream->ic->pb);
					if (cur_stream->ic->bit_rate)
						incr *= cur_stream->ic->bit_rate / 8.0;
					else
						incr *= 180000.0;
					pos += incr;
					stream_seek(cur_stream, pos, incr, 1);
				} else {
					pos = get_master_clock(cur_stream);
					pos += incr;
					stream_seek(cur_stream, (int64_t) (pos * AV_TIME_BASE),
							(int64_t) (incr * AV_TIME_BASE), 0);
				}
				break;
			default:
				break;
			}
			break;
		case SDL_VIDEOEXPOSE:
			cur_stream->force_refresh = 1;
			break;
		case SDL_MOUSEBUTTONDOWN:
			if (exit_on_mousedown) {
				do_exit(cur_stream);
				break;
			}
		case SDL_WINDOWEVENT_RESIZED:
			break;
		case SDL_MOUSEMOTION:
			if (event.type == SDL_MOUSEBUTTONDOWN) {
				x = event.button.x;
			} else {
				if (event.motion.state != SDL_PRESSED)
					break;
				x = event.motion.x;
			}
			if (seek_by_bytes || cur_stream->ic->duration <= 0) {
				uint64_t size = avio_size(cur_stream->ic->pb);
				stream_seek(cur_stream, size * x / cur_stream->width, 0, 1);
			} else {
				int64_t ts;
				int ns, hh, mm, ss;
				int tns, thh, tmm, tss;
				tns = cur_stream->ic->duration / 1000000LL;
				thh = tns / 3600;
				tmm = (tns % 3600) / 60;
				tss = (tns % 60);
				frac = x / cur_stream->width;
				ns = frac * tns;
				hh = ns / 3600;
				mm = (ns % 3600) / 60;
				ss = (ns % 60);
				__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
						"Seek to %2.0f%% (%2d:%02d:%02d) of total duration (%2d:%02d:%02d)       \n",
						frac * 100, hh, mm, ss, thh, tmm, tss);
				ts = frac * cur_stream->ic->duration;
				if (cur_stream->ic->start_time != AV_NOPTS_VALUE)
					ts += cur_stream->ic->start_time;
				stream_seek(cur_stream, ts, 0, 0);
			}
			break;

		case SDL_QUIT:
			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
					"SDL_QUIT===================\n");
			SDL_Quit();
		case FF_QUIT_EVENT:
			do_exit(cur_stream);
			break;
		case FF_ALLOC_EVENT:
			alloc_picture(event.user.data1);
			break;
		case FF_REFRESH_EVENT:
			video_refresh(event.user.data1);
			cur_stream->refresh = 0;
			break;
		case KU_PLAYER_SEEK:
			__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
					"seek %d msec\n", event.user.data1);
			int64_t seek_pos = (int64_t) 1000 * (int) event.user.data1;
			stream_seek(cur_stream, seek_pos, 0, 0);
			break;
		case KU_PLAYER_PAUSE:
			toggle_pause(cur_stream);
		case KU_PLAYER_BUFCHK:
			if (cur_stream->load) {
				if (check_buffer(cur_stream, MIN_FRAMES)) {
#ifdef ANDORID
					Android_Notify(MSG_LOAD_FINISHED);
#else
					stream_toggle_pause(cur_stream);
#endif
					cur_stream->load = 0;
				}

			} else {
				//Playering
				if (!check_buffer(cur_stream, 1)) {
#ifdef ANDROID
//					Android_Notify(MSG_LOAD_UNFINISHED);
#else
					stream_toggle_pause(cur_stream);
#endif
					cur_stream->load = 1;
				}
			}
		default:
			break;
		}
	}
}

static int lockmgr(void **mtx, enum AVLockOp op) {
	switch (op) {
	case AV_LOCK_CREATE:
		*mtx = SDL_CreateMutex();
		if (!*mtx)
			return 1;
		return 0;
	case AV_LOCK_OBTAIN:
		return !!SDL_LockMutex(*mtx);
	case AV_LOCK_RELEASE:
		return !!SDL_UnlockMutex(*mtx);
	case AV_LOCK_DESTROY:
		SDL_DestroyMutex(*mtx);
		return 0;
	}
	return 1;
}

int player_init() {
	int flags;
	av_log_set_flags(AV_LOG_SKIP_REPEATED);
	/* register all codecs, demux and protocols */
	avcodec_register_all();
#if CONFIG_AVDEVICE
	avdevice_register_all();
#endif

	av_register_all();
	avformat_network_init();

	signal(SIGINT, sigterm_handler); /* Interrupt (ANSI).    */
	signal(SIGTERM, sigterm_handler); /* Termination (ANSI).  */

	if (display_disable) {
		video_disable = 1;
	}
	flags = SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER;
	//if (audio_disable)
	//    flags &= ~SDL_INIT_AUDIO;
#if !defined(__MINGW32__) && !defined(__APPLE__)
	//flags |= SDL_INIT_EVENTTHREAD; /* Not supported on Windows or Mac OS X */
#endif
	if (SDL_Init(flags)) {
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"Could not initialize SDL - %s\n", SDL_GetError());
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"(Did you set the DISPLAY variable?)\n");
		return RET_INIT_ERROR;
	}

	SDL_SetHintWithPriority("SDL_RENDER_SCALE_QUALITY", "2", SDL_HINT_OVERRIDE);

#ifdef ANDROID
	if (!display_disable) {
		const SDL_VideoInfo *vi = SDL_GetVideoInfo();
		fs_screen_width = vi->current_w;
		fs_screen_height = vi->current_h;
	}
#endif

	if (fs_screen_width <= 0 || fs_screen_height <= 0) {
		fs_screen_width = 800;
		fs_screen_height = 480;
	}

	SDL_EventState(SDL_ACTIVEEVENT, SDL_IGNORE);
	SDL_EventState(SDL_SYSWMEVENT, SDL_IGNORE);
	SDL_EventState(SDL_USEREVENT, SDL_IGNORE);

	if (av_lockmgr_register(lockmgr)) {
		__android_log_print(ANDROID_LOG_VERBOSE, "anvizplayer_android",
				"Could not initialize lock manager!\n");
		return RET_INIT_ERROR;
	}

	av_init_packet(&flush_pkt);
	flush_pkt.data = (uint8_t *) (intptr_t) "FLUSH";
	return RET_OK;

}

int player_prepare(const char *url) {
	return stream_open(url, NULL);
}

int player_main() {
	event_loop(g_stream);
	return RET_OK;
}

int player_exit() {
	do_exit(g_stream);
	return RET_OK;
}

int main(int argc, char **argv) {

}

int getDuration() {
	return (int) g_total_duration;
}

int getCurrentPosition() {
	return g_current_duration;
}

int seekTo(int msec) {
	SDL_Event event;
	event.type = KU_PLAYER_SEEK;
	event.user.data1 = (void*) msec;
	SDL_PushEvent(&event);
	return 0;
}

//jni调用
int streamPause() {
	SDL_Event event;
	event.type = KU_PLAYER_PAUSE;
	SDL_PushEvent(&event);
	return 0;
}

int isPlay() {
	if (g_stream)
		return !(g_stream->paused);
	return 0;
}
