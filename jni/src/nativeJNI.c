#include <string.h>
#include <jni.h>
#include <unistd.h>
#include <android/log.h>

//取得设备列表
jstring Java_org_libsdl_app_SDLActivity_getDevList(JNIEnv* env, jobject thiz,
		jint jindex) {

	int index = jindex;
	char * json = getDevList(jindex);
	return (*env)->NewStringUTF(env, json);
}

//初始化连接TUTK服务器
int Java_org_libsdl_app_SDLActivity_initTutk(JNIEnv* env, jobject thiz,
		jstring juid) {
	char uid[128];
	const char *str = (*env)->GetStringUTFChars(env, juid, 0);
	strncpy(uid, str, 128);
	(*env)->ReleaseStringUTFChars(env, juid, str);
	initTutk(uid);
}

//开始连接设备
int Java_org_libsdl_app_SDLActivity_startDev(JNIEnv* env, jobject thiz,
		jint jmid) {
	int mid = jmid;
	startDev(mid);
}

//准备播放
int Java_org_libsdl_app_SDLActivity_PlayerPrepare(JNIEnv* env, jobject thiz,
		jstring jfileName) {
	char localFileName[1024];
	const char *str = (*env)->GetStringUTFChars(env, jfileName, 0);
	strncpy(localFileName, str, 1024);
	(*env)->ReleaseStringUTFChars(env, jfileName, str);
	return player_prepare(localFileName);

//	C++ jstring->char * cpp后缀文件
//	jboolean isCopy;
//	char localFileName[1024];
//	const char *fileString = env->GetStringUTFChars(jfileName, &isCopy);
//	strncpy(localFileName, fileString, 1024);
//	env->ReleaseStringUTFChars(jfileName, fileString);
//	return player_prepare(localFileName);

}

int Java_org_libsdl_app_SDLActivity_PlayerInit(JNIEnv* env, jobject obj) {
	return player_init();
}

int Java_org_libsdl_app_SDLActivity_PlayerMain(JNIEnv* env, jobject obj) {
	return player_main();
}

int Java_org_libsdl_app_SDLActivity_PlayerExit(JNIEnv* env, jobject obj) {
	return player_exit();
}

int Java_org_libsdl_app_SDLActivity_PlayerSeekTo(JNIEnv* env, jobject obj,
		jint msec) {
	int pos = msec;
	return seekTo(pos);
}

int Java_org_libsdl_app_SDLActivity_PlayerPause(JNIEnv* env, jobject obj) {
	return streamPause();
}

int Java_org_libsdl_app_SDLActivity_PlayerIsPlay(JNIEnv* env, jobject obj) {
	return isPlay();
}

int Java_org_libsdl_app_SDLActivity_PlayerGetDuration(JNIEnv* env, jobject obj) {
	return getDuration();
}

int Java_org_libsdl_app_SDLActivity_PlayergetCurrentPosition(JNIEnv* env,
		jobject obj) {
	return getCurrentPosition();
}

//屏幕横竖切换时调用
void Java_org_libsdl_app_SDLActivity_isChange(JNIEnv* env, jobject thiz,
		jint jwidth, jint jheight) {
	int width = jwidth;
	int height = jheight;
	is_change(width, height);
}
//TUTK命令调用，返回json格式字符串
jstring Java_org_libsdl_app_SDLActivity_visit(JNIEnv* env, jobject thiz, jstring jcomm){
	char comm[128];
	const char *str = (*env)->GetStringUTFChars(env, jcomm, 0);
	strncpy(comm, str, 128);
	(*env)->ReleaseStringUTFChars(env, jcomm, str);
	__android_log_print(ANDROID_LOG_VERBOSE, "nativeJNI", "COMM = %s\n", comm);

	char * resp = visit(comm);
//	char * resp = "{\"SYS_GET_DEVINFO\":{\"productmodel\":\"model\",\"softver\":\"softver\",\"hardver\":\"hardver\",\"mac\":\"mac\",\"uuid\":\"uuid\",\"maxchannel\":\"maxch\",\"maxstream\":\"maxstream\"}}";
	return (*env)->NewStringUTF(env, resp);
}
