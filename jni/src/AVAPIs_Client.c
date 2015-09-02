#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <time.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <fcntl.h>
#include "IOTCAPIs.h"
#include "AVAPIs.h"
#include "AVFRAMEINFO.h"
#include "AVIOCTRLDEFs.h"
#include <android/log.h>

#define SERVTYPE_STREAM_SERVER	1
#define AUDIO_STREAM_OUT_CH	1
#define MAX_SIZE_IOCTRL_BUF		1024

#define VIDEO_RECORD_FRAMES 400
#define AUDIO_RECORD_FRAMES 800

#define AUDIO_BUF_SIZE	1024

#define AUDIO_SPEAKER_CHANNEL 5

void PrintErrHandling(int nErr) {
	switch (nErr) {
	case IOTC_ER_SERVER_NOT_RESPONSE:
		//-1 IOTC_ER_SERVER_NOT_RESPONSE
		printf("[Error code : %d]\n", IOTC_ER_SERVER_NOT_RESPONSE);
		printf("Master doesn't respond.\n");
		printf(
				"Please check the network wheather it could connect to the Internet.\n");
		break;
	case IOTC_ER_FAIL_RESOLVE_HOSTNAME:
		//-2 IOTC_ER_FAIL_RESOLVE_HOSTNAME
		printf("[Error code : %d]\n", IOTC_ER_FAIL_RESOLVE_HOSTNAME);
		printf("Can't resolve hostname.\n");
		break;
	case IOTC_ER_ALREADY_INITIALIZED:
		//-3 IOTC_ER_ALREADY_INITIALIZED
		printf("[Error code : %d]\n", IOTC_ER_ALREADY_INITIALIZED);
		printf("Already initialized.\n");
		break;
	case IOTC_ER_FAIL_CREATE_MUTEX:
		//-4 IOTC_ER_FAIL_CREATE_MUTEX
		printf("[Error code : %d]\n", IOTC_ER_FAIL_CREATE_MUTEX);
		printf("Can't create mutex.\n");
		break;
	case IOTC_ER_FAIL_CREATE_THREAD:
		//-5 IOTC_ER_FAIL_CREATE_THREAD
		printf("[Error code : %d]\n", IOTC_ER_FAIL_CREATE_THREAD);
		printf("Can't create thread.\n");
		break;
	case IOTC_ER_UNLICENSE:
		//-10 IOTC_ER_UNLICENSE
		printf("[Error code : %d]\n", IOTC_ER_UNLICENSE);
		printf("This UID is unlicense.\n");
		printf("Check your UID.\n");
		break;
	case IOTC_ER_NOT_INITIALIZED:
		//-12 IOTC_ER_NOT_INITIALIZED
		printf("[Error code : %d]\n", IOTC_ER_NOT_INITIALIZED);
		printf("Please initialize the IOTCAPI first.\n");
		break;
	case IOTC_ER_TIMEOUT:
		//-13 IOTC_ER_TIMEOUT
		break;
	case IOTC_ER_INVALID_SID:
		//-14 IOTC_ER_INVALID_SID
		printf("[Error code : %d]\n", IOTC_ER_INVALID_SID);
		printf("This SID is invalid.\n");
		printf("Please check it again.\n");
		break;
	case IOTC_ER_EXCEED_MAX_SESSION:
		//-18 IOTC_ER_EXCEED_MAX_SESSION
		printf("[Error code : %d]\n", IOTC_ER_EXCEED_MAX_SESSION);
		printf("[Warning]\n");
		printf("The amount of session reach to the maximum.\n");
		printf("It cannot be connected unless the session is released.\n");
		break;
	case IOTC_ER_CAN_NOT_FIND_DEVICE:
		//-19 IOTC_ER_CAN_NOT_FIND_DEVICE
		printf("[Error code : %d]\n", IOTC_ER_CAN_NOT_FIND_DEVICE);
		printf("Device didn't register on server, so we can't find device.\n");
		printf("Please check the device again.\n");
		printf("Retry...\n");
		break;
	case IOTC_ER_SESSION_CLOSE_BY_REMOTE:
		//-22 IOTC_ER_SESSION_CLOSE_BY_REMOTE
		printf("[Error code : %d]\n", IOTC_ER_SESSION_CLOSE_BY_REMOTE);
		printf("Session is closed by remote so we can't access.\n");
		printf("Please close it or establish session again.\n");
		break;
	case IOTC_ER_REMOTE_TIMEOUT_DISCONNECT:
		//-23 IOTC_ER_REMOTE_TIMEOUT_DISCONNECT
		printf("[Error code : %d]\n", IOTC_ER_REMOTE_TIMEOUT_DISCONNECT);
		printf(
				"We can't receive an acknowledgement character within a TIMEOUT.\n");
		printf("It might that the session is disconnected by remote.\n");
		printf("Please check the network wheather it is busy or not.\n");
		printf("And check the device and user equipment work well.\n");
		break;
	case IOTC_ER_DEVICE_NOT_LISTENING:
		//-24 IOTC_ER_DEVICE_NOT_LISTENING
		printf("[Error code : %d]\n", IOTC_ER_DEVICE_NOT_LISTENING);
		printf(
				"Device doesn't listen or the sessions of device reach to maximum.\n");
		printf(
				"Please release the session and check the device wheather it listen or not.\n");
		break;
	case IOTC_ER_CH_NOT_ON:
		//-26 IOTC_ER_CH_NOT_ON
		printf("[Error code : %d]\n", IOTC_ER_CH_NOT_ON);
		printf("Channel isn't on.\n");
		printf(
				"Please open it by IOTC_Session_Channel_ON() or IOTC_Session_Get_Free_Channel()\n");
		printf("Retry...\n");
		break;
	case IOTC_ER_SESSION_NO_FREE_CHANNEL:
		//-31 IOTC_ER_SESSION_NO_FREE_CHANNEL
		printf("[Error code : %d]\n", IOTC_ER_SESSION_NO_FREE_CHANNEL);
		printf("All channels are occupied.\n");
		printf("Please release some channel.\n");
		break;
	case IOTC_ER_TCP_TRAVEL_FAILED:
		//-32 IOTC_ER_TCP_TRAVEL_FAILED
		printf("[Error code : %d]\n", IOTC_ER_TCP_TRAVEL_FAILED);
		printf("Device can't connect to Master.\n");
		printf("Don't let device use proxy.\n");
		printf("Close firewall of device.\n");
		printf("Or open device's TCP port 80, 443, 8080, 8000, 21047.\n");
		break;
	case IOTC_ER_TCP_CONNECT_TO_SERVER_FAILED:
		//-33 IOTC_ER_TCP_CONNECT_TO_SERVER_FAILED
		printf("[Error code : %d]\n", IOTC_ER_TCP_CONNECT_TO_SERVER_FAILED);
		printf("Device can't connect to server by TCP.\n");
		printf("Don't let server use proxy.\n");
		printf("Close firewall of server.\n");
		printf("Or open server's TCP port 80, 443, 8080, 8000, 21047.\n");
		printf("Retry...\n");
		break;
	case IOTC_ER_NO_PERMISSION:
		//-40 IOTC_ER_NO_PERMISSION
		printf("[Error code : %d]\n", IOTC_ER_NO_PERMISSION);
		printf("This UID's license doesn't support TCP.\n");
		break;
	case IOTC_ER_NETWORK_UNREACHABLE:
		//-41 IOTC_ER_NETWORK_UNREACHABLE
		printf("[Error code : %d]\n", IOTC_ER_NETWORK_UNREACHABLE);
		printf("Network is unreachable.\n");
		printf("Please check your network.\n");
		printf("Retry...\n");
		break;
	case IOTC_ER_FAIL_SETUP_RELAY:
		//-42 IOTC_ER_FAIL_SETUP_RELAY
		printf("[Error code : %d]\n", IOTC_ER_FAIL_SETUP_RELAY);
		printf(
				"Client can't connect to a device via Lan, P2P, and Relay mode\n");
		break;
	case IOTC_ER_NOT_SUPPORT_RELAY:
		//-43 IOTC_ER_NOT_SUPPORT_RELAY
		printf("[Error code : %d]\n", IOTC_ER_NOT_SUPPORT_RELAY);
		printf("Server doesn't support UDP relay mode.\n");
		printf("So client can't use UDP relay to connect to a device.\n");
		break;

	default:
		break;
	}
}

void close_dsp(int fd) {
	close(fd);
}

int open_dsp() {
	unlink("audio.in");
	return open("audio.in", O_RDWR | O_CREAT, 644);
}

void audio_playback(int fd, char *buf, int size) {
	int ret = write(fd, buf, size);
	if (ret < 0) {
		printf("audio_playback::write , ret=[%d]\n", ret);
	}
}

void *thread_Speaker(void *arg) {
	int SID = *(int *) arg;
	char buf[AUDIO_BUF_SIZE];
	int frameRate = 20;
	int sleepTick = 1000000 / frameRate;
	int sendCnt = 0;
	FILE *fp = fopen("audio.out", "rb");
	if (fp == NULL) {
		printf("fopen error!!!\n");
		return 0;
	}

	FRAMEINFO_t frameInfo;
	memset(&frameInfo, 0, sizeof(frameInfo));
	printf("Free CH[%d]\n", IOTC_Session_Get_Free_Channel(SID));
	int avIndex = avServStart(SID, NULL, NULL, 5000, 0, AUDIO_SPEAKER_CHANNEL);
	if (avIndex < 0) {
		printf("avServStart failed[%d]\n", avIndex);
		return 0;
	}
	printf("[thread_Speaker] Starting avIndex[%d]....\n", avIndex);

	frameInfo.codec_id = MEDIA_CODEC_AUDIO_SPEEX;
	frameInfo.flags = (AUDIO_SAMPLE_8K << 2) | (AUDIO_DATABITS_16 << 1)
			| AUDIO_CHANNEL_MONO;

	while (1) {
		int size = fread(buf, 1, 38, fp);
		if (size <= 0) {
			printf("rewind audio\n");
			rewind(fp);
			continue;
		}

		int ret = avSendAudioData(avIndex, buf, 38, &frameInfo,
				sizeof(FRAMEINFO_t));
		if (ret == AV_ER_SESSION_CLOSE_BY_REMOTE) {
			printf("thread_AudioFrameData AV_ER_SESSION_CLOSE_BY_REMOTE\n");
			break;
		} else if (ret == AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
			printf("thread_AudioFrameData AV_ER_REMOTE_TIMEOUT_DISCONNECT\n");
			break;
		} else if (ret == IOTC_ER_INVALID_SID) {
			printf("Session cant be used anymore\n");
			break;
		} else if (ret < 0) {
			printf("avSendAudioData error[%d]\n", ret);
			break;
		}
		usleep(sleepTick);
		if (sendCnt++ > 1500)
			break;
	}

	fclose(fp);
	printf("[thread_Speaker] exit...\n");
	avServStop(avIndex);

	return 0;
}

int gm_flag = 0;
void *thread_ReceiveAudio(void *arg) {
	printf("[thread_ReceiveAudio] Starting....\n");

	int avIndex = *(int *) arg;
	char buf[AUDIO_BUF_SIZE] = { 0 };
	int fd = open_dsp();
	if (fd < 0) {
		printf("open_dsp failed[%d]\n", fd);
		return 0;
	}

	FRAMEINFO_t frameInfo;
	unsigned int frmNo;
	int recordCnt = 0;
	int ret;
	printf("Start IPCAM audio stream OK!\n");

	while (1) {
		ret = avCheckAudioBuf(avIndex);
		if (ret < 0)
			break;
		if (ret < 30) // determined by audio frame rate
				{
			usleep(10000);
			continue;
		}

		ret = avRecvAudioData(avIndex, buf, AUDIO_BUF_SIZE, (char *) &frameInfo,
				sizeof(FRAMEINFO_t), &frmNo);

		if (ret == AV_ER_SESSION_CLOSE_BY_REMOTE) {
			printf("[thread_ReceiveAudio] AV_ER_SESSION_CLOSE_BY_REMOTE\n");
			break;
		} else if (ret == AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
			printf("[thread_ReceiveAudio] AV_ER_REMOTE_TIMEOUT_DISCONNECT\n");
			break;
		} else if (ret == IOTC_ER_INVALID_SID) {
			printf("[thread_ReceiveAudio] Session cant be used anymore\n");
			break;
		} else if (ret == AV_ER_LOSED_THIS_FRAME) {
			//printf("Audio AV_ER_LOSED_THIS_FRAME[%d]\n", frmNo);
			continue;
		} else if (ret < 0) {
			printf("Other error[%d]!!!\n", ret);
			continue;
		}

		//audio_playback(fd, buf, ret);
		if (recordCnt++ > AUDIO_RECORD_FRAMES)
			break;
		printf(".");
	}

	close_dsp(fd);
	printf("[thread_ReceiveAudio] thread exit\n");

	return 0;
}
char *UID[4];
#define VIDEO_BUF_SIZE	(1024*300)
FILE *fp = NULL;
int gm_index = -1;

int iflag = 0;
char iBuff[VIDEO_BUF_SIZE];
int iSize = 0;

int gSleepMs = 35;
FILE *file;

int avIndex2 = -1;

#define TP_DEV_MAX_LEN 64
#define TP_DEV_LIST_NUM 4
#define TP_DEV_RTSPFILE_LEN 30
#define TP_MAX_UUID_LEN 33

typedef struct _st_TP_DEVLIST {
	int m_ID;
	unsigned int uiIp;
	unsigned short usPort;
	unsigned short usRtspPort;
	char szRtspFile0[TP_DEV_RTSPFILE_LEN];
	char szRtspFile1[TP_DEV_RTSPFILE_LEN];
	char szDevInfo[TP_DEV_MAX_LEN];

} ST_TP_DEVLIST;

typedef struct _st_TP_IOCTRL_INFO {
	int iIndex;
	ST_TP_DEVLIST stTpDevList[TP_DEV_LIST_NUM];
} ST_TP_IOCTRL_INFO;

//发送命令给TUTK，前提需要调用initTutk(const char *uid)初始化TUTK
char * visit(char * comm) {

	int avIndex = *((int *) &avIndex2);
	unsigned int ioType;
	int ret;
	char stIOCtrl[1024];
	while (1) {
		if ((ret = avSendIOCtrl(avIndex, IOTYPE_TUTK_GET_PREW_DEV_LIST, comm,
				strlen(comm))) < 0) {
			continue;
		}
		ret = avRecvIOCtrl(avIndex, &ioType, (char *) &stIOCtrl,
				strlen(stIOCtrl), 3000000);
		if (ret >= 0) {
			gm_index = avIndex;
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
					"test\r\n");

			return stIOCtrl;
		} else if (ret != AV_ER_TIMEOUT) {
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
					"avRecvIOCtrl error, code[%d]\n", ret);
			continue;
		}

		usleep(1);
	}

}

//取得设备列表
char * getDevList(int iIndex) {
	ST_TP_IOCTRL_INFO stIOCtrl;
	int avIndex = *((int *) &avIndex2);
	unsigned int ioType;
	int ret;
	char sz_ID[64];
	char szIndex[64];
	char szForJni[2048];
	char szTmp[128];
	while (1) {
		memset(szIndex, 0, 64);
		sprintf(szIndex, "%d", iIndex);
		if ((ret = avSendIOCtrl(avIndex, IOTYPE_TUTK_GET_PREW_DEV_LIST,
				(char *) szIndex, strlen(szIndex)) < 0)) {
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
					"start_ipcam_stream failed[%d]  szIndex = %s\n", ret,
					szIndex);
			continue;
		}

		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
				"IOTYPE_INNER_SND_DATA_DELAY OK\n");

		ret = avRecvIOCtrl(avIndex, &ioType, (char *) &stIOCtrl,
				sizeof(ST_TP_IOCTRL_INFO), 3000000);
		if (ret >= 0) {
			gm_index = avIndex;
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
					"test\r\n");
			break;
		} else if (ret != AV_ER_TIMEOUT) {
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
					"avRecvIOCtrl error, code[%d]\n", ret);
			continue;
		}

		usleep(1);
	}

	memset(szForJni, 0, 2048);

	sprintf(szForJni, "{\"iIndex\":%d,\"stTpDevList\":[", stIOCtrl.iIndex);
	int i;
	for (i = 0; i < TP_DEV_LIST_NUM; i++) {

		sprintf(szTmp, "{\"m_ID\": %d,", stIOCtrl.stTpDevList[i].m_ID);
		strcat(szForJni, szTmp);
		sprintf(szTmp, "\"uiIp\": %d,", stIOCtrl.stTpDevList[i].uiIp);
		strcat(szForJni, szTmp);
		sprintf(szTmp, "\"usPort\": %d,", stIOCtrl.stTpDevList[i].usPort);
		strcat(szForJni, szTmp);
		sprintf(szTmp, "\"usRtspPort\": %d,",
				stIOCtrl.stTpDevList[i].usRtspPort);
		strcat(szForJni, szTmp);
		sprintf(szTmp, "\"szRtspFile0\": \"%s\",",
				stIOCtrl.stTpDevList[i].szRtspFile0);
		strcat(szForJni, szTmp);
		sprintf(szTmp, "\"szRtspFile1\": \"%s\",",
				stIOCtrl.stTpDevList[i].szRtspFile1);
		strcat(szForJni, szTmp);
		sprintf(szTmp, "\"szDevInfo\": \"%s\"}",
				stIOCtrl.stTpDevList[i].szDevInfo);
		strcat(szForJni, szTmp);
		if ((i + 1) != TP_DEV_LIST_NUM) {
			sprintf(szTmp, ",");
			strcat(szForJni, szTmp);
		}
	}
	sprintf(szTmp, "]}");
	strcat(szForJni, szTmp);

	return szForJni;
}

//开始连接设备
int startDev(int m_ID) {
	int avIndex = *((int *) &avIndex2);
	unsigned int ioType;
	int ret;
	char sz_ID[64];
	//free(arg);

	sprintf(sz_ID, "%d", m_ID);

	while ((ret = avSendIOCtrl(avIndex, IOTYPE_USER_IPCAM_START, sz_ID,
			strlen(sz_ID)) < 0)) {
		printf("start_ipcam_stream failed[%d]\n", ret);

		usleep(500);
		continue;
	}
}

struct timeval tv;

void *thread_ConnectCCR(void *arg) {
	int ret;
	int SID;
	char *UID = (char *) arg;
	int tmpSID = IOTC_Get_SessionID();
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"  [] thread_ConnectCCR::IOTC_Get_SessionID, ret=[%d]\n", tmpSID);
	if (tmpSID < 0) {
		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
				"IOTC_Get_SessionID failed[%d]\n", tmpSID);
		return 0;
	}

	//SID = IOTC_Connect_ByUID(UID);
	//SID = IOTC_Connect_ByName("TUTKCOMP", "888888");
	SID = IOTC_Connect_ByUID_Parallel(UID, tmpSID);
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client", "UID=====[%s]",
			UID);
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client", "tmpSID=====[%d]",
			tmpSID);
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"  [] thread_ConnectCCR::IOTC_Connect_ByUID_Parallel, ret=[%d]\n",
			SID);
	if (SID < 0) {
		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
				"IOTC_Connect_ByUID_Parallel failed[%d]\n", SID);
		return 0;
	}

	struct timeval tv2;
	gettimeofday(&tv2, NULL);
	long sec = tv2.tv_sec - tv.tv_sec, usec = tv2.tv_usec - tv.tv_usec;
	if (usec < 0) {
		sec--;
		usec += 1000000;
	}
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"SID[%d] Cost time = %ld sec, %ld ms\n", SID, sec, usec);

	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"Step 2: call IOTC_Connect_ByUID(%s) ret(%d).......\n", UID, SID);
	struct st_SInfo Sinfo;
	memset(&Sinfo, 0, sizeof(struct st_SInfo));
	IOTC_Session_Check(SID, &Sinfo);
	char *mode[] = { "P2P", "RLY", "LAN" };
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"Device is from %s:%d[%s] Mode=%s NAT[%d] IOTCVersion[%lX]\n",
			Sinfo.RemoteIP, Sinfo.RemotePort, Sinfo.UID, mode[(int) Sinfo.Mode],
			Sinfo.NatType, Sinfo.IOTCVersion);

	int nResend = -1;
	unsigned long srvType;
	int avIndex = avClientStart2(SID, "admin", "888888", 20000, &srvType, 0,
			&nResend);
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"Step 2: call avClientStart2(%d).......\n", avIndex);
	if (avIndex < 0) {
		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
				"avClientStart2 failed[%d]\n", avIndex);
		avDeInitialize();
		IOTC_DeInitialize();
		return 0;
	}
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"avClientStart2 OK[%d], Resend[%d]\n", avIndex, nResend);

	avIndex2 = avIndex;

	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"  [] thread_ConnectCCR::start_ipcam_stream \n");
	pthread_t ThreadVideo_ID;
	return NULL;
}
int getReceiveVideo(char *buf, int *iSize) {
	//printf("[thread_ReceiveVideo] Starting....\n");

	int ret;

	FRAMEINFO_t frameInfo;
	unsigned int frmNo;
//	printf("Start IPCAM video stream OK![%d]\n", avIndex);
	int flag = 0, cnt = 0;
	int outBufSize = 0;
	int outFrmSize = 0;
	int outFrmInfoSize = 0;

	int fpsCnt = 0, round = 0, lostCnt = 0;
	int bps = 0;

	if (gm_index == -1) {
		return 0;
	}

#if 0
	ret = avRecvFrameData(gm_index, buf, VIDEO_BUF_SIZE, (char *)&frameInfo, sizeof(FRAMEINFO_t), &frmNo);
#else
	ret = avRecvFrameData2(gm_index, buf, VIDEO_BUF_SIZE, &outBufSize,
			&outFrmSize, (char *) &frameInfo, sizeof(FRAMEINFO_t),
			&outFrmInfoSize, &frmNo);
#endif

//	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
//			"RET------------[%d]", ret);

	if (ret == AV_ER_DATA_NOREADY) {
		//printf("AV_ER_DATA_NOREADY[%d]\n", avIndex);
		usleep(1000 * 10);
		return 0;
	} else if (ret == AV_ER_LOSED_THIS_FRAME) {
		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
				"Lost video frame NO[%d]\n", frmNo);
		lostCnt++;
		//continue;
	} else if (ret == AV_ER_INCOMPLETE_FRAME) {
#if 1
		if (outFrmInfoSize > 0)
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
					"Incomplete video frame NO[%d] ReadSize[%d] FrmSize[%d] FrmInfoSize[%u] Codec[%d] Flag[%d]\n",
					frmNo, outBufSize, outFrmSize, outFrmInfoSize,
					frameInfo.codec_id, frameInfo.flags);
		else
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
					"Incomplete video frame NO[%d] ReadSize[%d] FrmSize[%d] FrmInfoSize[%u]\n",
					frmNo, outBufSize, outFrmSize, outFrmInfoSize);
#endif
		lostCnt++;
		//continue;
	} else if (ret == AV_ER_SESSION_CLOSE_BY_REMOTE) {
		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
				"[thread_ReceiveVideo] AV_ER_SESSION_CLOSE_BY_REMOTE\n");
		return 0;
	} else if (ret == AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
				"[thread_ReceiveVideo] AV_ER_REMOTE_TIMEOUT_DISCONNECT\n");
		thread_ConnectCCR((void *) UID[0]);

		return 0;
	} else if (ret == IOTC_ER_INVALID_SID) {
		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
				"[thread_ReceiveVideo] Session cant be used anymore\n");
		return 0;
	} else {
		bps += outBufSize;
#if 0
		static int frmCnt = 0;
		char fn[32];
		if(frameInfo.flags == IPC_FRAME_FLAG_IFRAME)
		sprintf(fn, "I_%03d.bin", frmCnt);
		else
		sprintf(fn, "P_%03d.bin", frmCnt);
		frmCnt++;
		FILE *fp = fopen(fn, "wb+");
		fwrite(buf, 1, outBufSize, fp);
		fclose(fp);
#endif
	}

	static int iiflag = 0;

	if (buf[4] == 0x67) {
		iiflag = 1;
		if (fp == NULL)
			fp = fopen("/mnt/sdcard/ipcam.h264", "wb+");
	}
	if (iiflag) {
		//memset(iBuff,0,VIDEO_BUF_SIZE);
		//memcpy(iBuff,buf, VIDEO_BUF_SIZE);
		*iSize = ret;
		printf("%d\r\n", ret);

		if (NULL != fp)
			fwrite(buf, 1, ret, fp);

		return 1;
	}

	//if(cnt > VIDEO_RECORD_FRAMES) break;
	return 0;

}
char UIDBuf[128];
int initTutk(const char *uid) {
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client", "开始\n");
	srand(time(NULL));

	int ret;

	int nNumUID = 1;
	int j;

	memset(UIDBuf, 0, 128);
	UID[0] = UIDBuf;
	strcpy(UID[0], uid);

	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT", "UID[%s]\n",
			UID[0]);

	gettimeofday(&tv, NULL);
	ret = IOTC_Initialize2(0);
	if (ret != IOTC_ER_NoERROR) {
		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
				"IOTCAPIs_Device exit...!!\n");
		PrintErrHandling(ret);
		return 0;
	}

	avInitialize(32);
	unsigned long iotcVer;
	IOTC_Get_Version(&iotcVer);
	int avVer = avGetAVApiVer();
	unsigned char *p = (unsigned char *) &iotcVer;
	unsigned char *p2 = (unsigned char *) &avVer;
	char szIOTCVer[16], szAVVer[16];
	sprintf(szIOTCVer, "%d.%d.%d.%d", p[3], p[2], p[1], p[0]);
	sprintf(szAVVer, "%d.%d.%d.%d", p2[3], p2[2], p2[1], p2[0]);
	printf("IOTCAPI version[%s] AVAPI version[%s]\n", szIOTCVer, szAVVer);
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIs_Client",
			"IOTCAPI version[%s] AVAPI version[%s]\n", szIOTCVer, szAVVer);

	int i;
	struct st_LanSearchInfo *psLanSearchInfo =
			(struct st_LanSearchInfo *) malloc(
					sizeof(struct st_LanSearchInfo) * 12);
	if (psLanSearchInfo != NULL) {
		// wait time 2000 ms to get result, if result is 0 you can extend to 3000 ms
		int nDeviceNum = IOTC_Lan_Search(psLanSearchInfo, 12, 2000);
		printf("IOTC_Lan_Search ret[%d]\n", nDeviceNum);
		for (i = 0; i < nDeviceNum; i++) {
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
					"UID[%s] Addr[%s:%d]\n", psLanSearchInfo[i].UID,
					psLanSearchInfo[i].IP, psLanSearchInfo[i].port);
		}
		free(psLanSearchInfo);
	}
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
			"LAN search done...\n");

	pthread_t ConnectThread_ID[4];
	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT", "nNumUID[%d]\n",
			nNumUID);
	for (j = 0; j < nNumUID; j++) {

		__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
				"UID====[%s]\n", UID[j]);
		if ((ret = pthread_create(&ConnectThread_ID[j], NULL,
				&thread_ConnectCCR, (void *) UID[j]))) {
			__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
					"pthread_create(ConnectThread_ID), ret=[%d]\n", ret);
			exit(-1);
		}
	}

	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
			"StreamClient 开始...\n");
	for (j = 0; j < nNumUID; j++) {
		pthread_join(ConnectThread_ID[j], NULL);
	}
//	getDevList(0);
//	avDeInitialize();
//	IOTC_DeInitialize();

	__android_log_print(ANDROID_LOG_VERBOSE, "AVAPIS_CLINT",
			"StreamClient exit...\n");

	return 0;
}

