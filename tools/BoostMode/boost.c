/* system/bin/test/Boost.c
**
** Example:boost -o 0xff -l 1500000 -b 1896000 -m 1456000 -i 560000 -d 267000 -h -u
**
** Copyright 2015, The Android Open Source Project
** author:diog.zhao@samsung.com
**
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/
#include <ctype.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/inotify.h>
#include <sys/mount.h>
#include <sys/resource.h>
#include <sys/stat.h>
#include <sys/statfs.h>
#include <sys/time.h>
#include <sys/uio.h>
#include <unistd.h>

#include <cutils/fs.h>
#include <cutils/hashmap.h>
#include <cutils/log.h>
#include <cutils/multiuser.h>
#include <private/android_filesystem_config.h>

#define ERROR(x...) printf(x)
#define INFO(x...) printf(x)

int usage(void)
{
    exit(1);
    return 0;
}

typedef enum{
	BOOST_START = 0,
	BOOST_ENABLED,
	BOOST_DISABLED,
	BOOST_END,
}boost_status_et;

#define FILE_BUFFER_SIZE 128
#define FREQ_ARRAY_SIZE 20
#define POLLING_INTERVAL 1
#define MAX_CPU_NUM 8

typedef struct {
	int valid_freqs[FILE_BUFFER_SIZE];
	int size;	
}array_freq_table;

/*Global parameters*/
static char *cpu_online_path_prefix = "/sys/devices/system/cpu/cpu";
static char *cpu_online_path_suffix ="/online";
static char *littecore_freq_table_path = "/sys/devices/system/cpu/cpufreq/mp-cpufreq/cluster0_freq_table";
static char *littecore_max_freq_path = "/sys/devices/system/cpu/cpufreq/mp-cpufreq/cluster0_max_freq";
static char *littecore_min_freq_path = "/sys/devices/system/cpu/cpufreq/mp-cpufreq/cluster0_min_freq";
static char *bigcore_freq_table_path = "/sys/devices/system/cpu/cpufreq/mp-cpufreq/cluster1_freq_table";
static char *bigcore_max_freq_path = "/sys/devices/system/cpu/cpufreq/mp-cpufreq/cluster1_max_freq";
static char *bigcore_min_freq_path = "/sys/devices/system/cpu/cpufreq/mp-cpufreq/cluster1_min_freq";
static char *mif_freq_table_path = "/sys/class/devfreq/exynos7-devfreq-mif/available_frequencies";
static char *mif_max_freq_path = "/sys/class/devfreq/exynos7-devfreq-mif/max_freq";
static char *mif_min_freq_path = "/sys/class/devfreq/exynos7-devfreq-mif/min_freq";
static char *int_freq_table_path = "/sys/class/devfreq/exynos7-devfreq-int/available_frequencies";
static char *int_max_freq_path = "/sys/class/devfreq/exynos7-devfreq-int/max_freq";
static char *int_min_freq_path = "/sys/class/devfreq/exynos7-devfreq-int/min_freq";
static char *disp_freq_table_path = "/sys/class/devfreq/exynos7-devfreq-disp/available_frequencies";
static char *disp_max_freq_path = "/sys/class/devfreq/exynos7-devfreq-disp/max_freq";
static char *disp_min_freq_path = "/sys/class/devfreq/exynos7-devfreq-disp/min_freq";
static char *threeD_freq_table_path = "/sys/devices/14ac0000.mali/dvfs_table";
static char *threeD_freq_path = "/sys/devices/14ac0000.mali/clock";
static char *threeD_dvfs_enable_path = "/sys/devices/14ac0000.mali/dvfs";
static char *hmp_boost_enable_path = "/sys/kernel/hmp/boost";
static char *tmu_disable_path = "/sys/class/thermal/thermal_zone0/mode";

array_freq_table littlecore_freq;
array_freq_table bigcore_freq;
array_freq_table mif_freq;
array_freq_table int_freq;
array_freq_table disp_freq;
array_freq_table threeD_freq;

boost_status_et cur_boost_status = BOOST_DISABLED;

int littlecore_maxfreq_back = 0;
int littlecore_minfreq_back = 0;
int bigcore_maxfreq_back = 0;
int bigcore_minfreq_back = 0;
int mif_maxfreq_back = 0;
int mif_minfreq_back = 0;
int int_maxfreq_back = 0;
int int_minfreq_back = 0;
int disp_maxfreq_back = 0;
int disp_minfreq_back = 0;
int hmp_boost_back = 0;
int threed_dvfs_enabled_back= 0;
int tmu_disable_back = 0;

int read_sys_file(char *pname, char * buf, int size)
{
	int fd;
	int realsize =0;
	fd = open(pname,O_RDONLY);
	realsize = read(fd,buf,size);
	buf[realsize] = 0;
	return realsize;
}

int write_sys_file(char *pname, char * buf, int size)
{
	int fd;
	fd = open(pname,O_WRONLY);
	write(fd,buf,size);
	return 0;
}

int init_freq_table(array_freq_table *pArray, char *freq_table_path)
{
		int size = 0;
		char buf[FILE_BUFFER_SIZE+1];	
		char *p = NULL;
		
		size = read_sys_file(freq_table_path, buf, FILE_BUFFER_SIZE);
		printf("freq_table_path=%s\n,size=%d, freq=%s", freq_table_path,size,buf);
		p= strtok(buf, " ");
		while(p && ('\n' != *p)){		
			pArray->valid_freqs[pArray->size++] = atoi(p);
			p = strtok(NULL, " ");
		}			
		return 0;
}

int  init_valid_parameters_tables()
{
		init_freq_table(&littlecore_freq, littecore_freq_table_path);		
		init_freq_table(&bigcore_freq, bigcore_freq_table_path);		
		init_freq_table(&mif_freq, mif_freq_table_path);		
		init_freq_table(&int_freq, int_freq_table_path);		
		init_freq_table(&disp_freq, disp_freq_table_path);		
		init_freq_table(&threeD_freq, threeD_freq_table_path);		
		return 0;		
}

bool check_input_freq(array_freq_table *pArray, int input_freq)
{
		int i = 0;
		
		for(i=0;i<pArray->size;i++){
			if(input_freq == pArray->valid_freqs[i])
				return true;
		}
		return false;
}
int get_valid_max_freq(array_freq_table *pArray, int *pInput_freq)
{
		int i = 0;
		
		*pInput_freq = 0;
		
		for(i=0;i<pArray->size;i++){			
			if(*pInput_freq < pArray->valid_freqs[i])
				*pInput_freq = pArray->valid_freqs[i];
		}
		return 0;	
}
#define CHECK_LITTLEFREQ(freq) check_input_freq(&littlecore_freq, freq)
#define CHECK_BIGFREQ(freq) check_input_freq(&bigcore_freq, freq)
#define CHECK_MIFFREQ(freq) check_input_freq(&mif_freq, freq)
#define CHECK_INTFREQ(freq) check_input_freq(&int_freq, freq)
#define CHECK_DISPFREQ(freq) check_input_freq(&disp_freq, freq)
#define CHECK_THREEDFREQ(freq) (check_input_freq(&threeD_freq, freq)|| !freq)

bool check_antutu_is_running(){
	/*Here should check aututu app is running or not*/
	return true;
}
int get_intvalue_fromsys(char *path, int *pValue)
{
		int size = 0;
		char buf[FILE_BUFFER_SIZE+1];	

		size = read_sys_file(path, buf, FILE_BUFFER_SIZE);
		printf("path=%s\n,size=%d, freq=%s", path,size,buf);

		*pValue = atoi(buf);
		return 0;		
}

int set_intvalue_fromsys(char *path, int Value)
{
		char tmpbuf[16];
		snprintf(tmpbuf, sizeof(tmpbuf), "%d", Value);
		printf("change path %s value to %d\n", path, Value);
		write_sys_file(path,tmpbuf,strlen(tmpbuf));	
		return 0;
}

int force_cpuonline_fromsys(int online)
{
	int i=0;
	char path[FILE_BUFFER_SIZE];
	char tmpbuf[16];
	for(;i<MAX_CPU_NUM;i++){
		if(online & 0x1){
			strcpy(path, cpu_online_path_prefix);
			snprintf(tmpbuf, sizeof(tmpbuf), "%d", i);
			strcat(path,tmpbuf);
			strcat(path, cpu_online_path_suffix);
			set_intvalue_fromsys(path, 1);
		}		
		online = online >> 1;
	}
	return 0;
}

int save_old_status()
{
	get_intvalue_fromsys(littecore_max_freq_path, &littlecore_maxfreq_back);
	get_intvalue_fromsys(littecore_min_freq_path, &littlecore_minfreq_back);
	get_intvalue_fromsys(bigcore_max_freq_path, &bigcore_maxfreq_back);
	get_intvalue_fromsys(bigcore_min_freq_path, &bigcore_minfreq_back);
	get_intvalue_fromsys(mif_max_freq_path, &mif_maxfreq_back);
	get_intvalue_fromsys(mif_min_freq_path, &mif_minfreq_back);
	get_intvalue_fromsys(int_max_freq_path, &int_maxfreq_back);
	get_intvalue_fromsys(int_min_freq_path, &int_minfreq_back);
	get_intvalue_fromsys(disp_max_freq_path, &disp_maxfreq_back);
	get_intvalue_fromsys(disp_min_freq_path, &disp_minfreq_back);
	get_intvalue_fromsys(threeD_dvfs_enable_path, &threed_dvfs_enabled_back);	
	get_intvalue_fromsys(hmp_boost_enable_path, &hmp_boost_back);
	return 0;
}

int restore_old_status()
{
	set_intvalue_fromsys(littecore_max_freq_path, littlecore_maxfreq_back);
	set_intvalue_fromsys(littecore_min_freq_path, littlecore_minfreq_back);
	set_intvalue_fromsys(bigcore_max_freq_path, bigcore_maxfreq_back);
	set_intvalue_fromsys(bigcore_min_freq_path, bigcore_minfreq_back);
	set_intvalue_fromsys(mif_max_freq_path, mif_maxfreq_back);
	set_intvalue_fromsys(mif_min_freq_path, mif_minfreq_back);
	set_intvalue_fromsys(int_max_freq_path, int_maxfreq_back);
	set_intvalue_fromsys(int_min_freq_path, int_minfreq_back);
	set_intvalue_fromsys(disp_max_freq_path, disp_maxfreq_back);
	set_intvalue_fromsys(disp_min_freq_path, disp_minfreq_back);
	set_intvalue_fromsys(threeD_dvfs_enable_path, threed_dvfs_enabled_back);	
	set_intvalue_fromsys(hmp_boost_enable_path, hmp_boost_back);
	return 0;
}
/* 0xff -l 1500000 -b 1896000 -m 1456000 -i 560000 -d 267000 -h -u*/
#define DEFAULT_ONLINE_MASK 0xff
#define DEFAULT_LITTLE_MIN 1500000
#define DEFAULT_BIG_MIN 1896000
#define DEFAULT_MIF_MIN 1456000
#define DEFAULT_INT_MIN 560000
#define DEFAULT_DISP_MIN 267000
/*boost -o 0xff -h -u*/

int main(int argc, char **argv)
{
	int res = 0;
	bool hmp_boost = false;
	bool tmu_disabled = false;/*invalid currently, not finished.*/
	int online_mask = 0;/*all online*/
	int little_min_freq = 0, little_max_freq = 0;
	int big_min_freq = 0, big_max_freq = 0;
	int mif_min_freq = 0, mif_max_freq = 0;
	int int_min_freq = 0, int_max_freq = 0;
	int disp_min_freq = 0, disp_max_freq = 0;
	int threeD_freq_value = 0;
	int i;

	int opt;
	while ((opt = getopt(argc, argv, "o:l:b:m:i:d:t:hu")) != -1) {
		switch (opt) {
			case 'h':/*hmp boost enable*/
				hmp_boost = true;
				break;
			case 'o':
				online_mask = strtoul(optarg, NULL, 0);
				break;
			case 'l':
				little_min_freq = strtoul(optarg, NULL, 0);
				//little_max_freq = strtoul(optarg, NULL, 0);
				break;
			case 'b':
				big_min_freq = strtoul(optarg, NULL, 0);
				//big_max_freq = strtoul(optarg, NULL, 0);
				break;
			case 'm':
				mif_min_freq = strtoul(optarg, NULL, 0);
				//mif_max_freq = strtoul(optarg, NULL, 0);
				break;
			case 'i':
				int_min_freq = strtoul(optarg, NULL, 0);
				//int_max_freq = strtoul(optarg, NULL, 0); 
				break;
			case 'd':
				disp_min_freq = strtoul(optarg, NULL, 0);
				//disp_max_freq = strtoul(optarg, NULL, 0);
				break; 
			case 't':
				threeD_freq_value = strtoul(optarg, NULL, 0);
				break;
			case 'u':
				tmu_disabled = true;
				break; 
			case '?':
				default:
			return usage();
		}
	}
	printf("%d,%d,\n%d,%d,\n%d,%d,\n%d,%d,\n%d,%d\n%d,%d,%d",little_min_freq,little_max_freq,
		big_min_freq,big_max_freq,mif_min_freq,mif_max_freq,int_min_freq,int_max_freq,
		disp_min_freq,disp_max_freq,hmp_boost,online_mask,threeD_freq_value);
	/*Here Init valid parameters tables from kernel by sys*/
	init_valid_parameters_tables();
	/*check parameters */
	if(!online_mask){
		ERROR("Invalid online mask\n");
		return usage();
	}
	if (!CHECK_LITTLEFREQ(little_min_freq)) {
		ERROR("Invalid littlecore min freq, use default\n");
		little_min_freq = DEFAULT_LITTLE_MIN;
		//return usage();
	}
	if (!CHECK_LITTLEFREQ(little_max_freq)) {
		INFO("Invalid littlecore max freq, set to default\n");
		get_valid_max_freq(&littlecore_freq, &little_max_freq);
		if(!little_max_freq)
			little_max_freq = little_min_freq;
	}
	if(!CHECK_BIGFREQ(big_min_freq)) {	  
		ERROR("Invalid bigcore freq,use default\n");
		big_min_freq = DEFAULT_BIG_MIN;
		//return usage();
	}
	if
	(!CHECK_BIGFREQ(big_max_freq)) {	  
		INFO("Invalid bigcore max freq, set to default\n");
		get_valid_max_freq(&bigcore_freq, &big_max_freq);
		if(!big_max_freq)
		 	big_max_freq = big_min_freq;
	}
	if(!CHECK_MIFFREQ(mif_min_freq)) {	  
		ERROR("Invalid mif freq,use default\n");
		mif_min_freq = DEFAULT_MIF_MIN;
		//return usage();
	}
	if(!CHECK_BIGFREQ(mif_max_freq)) {	  
		INFO("Invalid mif max freq, set to default\n");
		get_valid_max_freq(&mif_freq, &mif_max_freq);
		if(!mif_max_freq)
		 	mif_max_freq = mif_min_freq;
	}
	if(!CHECK_INTFREQ(int_min_freq)) {	  
		ERROR("Invalid int freq,use default\n");
		int_min_freq = DEFAULT_INT_MIN;
		//return usage();
	}
	if(!CHECK_INTFREQ(int_max_freq)) {	  
		INFO("Invalid int max freq, set to default\n");
		get_valid_max_freq(&int_freq, &int_max_freq);
		if(!int_max_freq)
		 	int_max_freq = int_min_freq;
	}
	if(!CHECK_DISPFREQ(disp_min_freq)) {	  
		ERROR("Invalid disp freq,use default\n");
		disp_min_freq = DEFAULT_DISP_MIN;
		//return usage();
	}
	if(!CHECK_DISPFREQ(disp_max_freq)) {	  
		INFO("Invalid disp max freq, set to default\n");
		get_valid_max_freq(&disp_freq, &disp_max_freq);
		if(!disp_max_freq)
		 	disp_max_freq = disp_min_freq;
	}
	if (!CHECK_THREEDFREQ(threeD_freq_value)) {
		ERROR("Invalid 3d freq\n");	  
		return usage();
	}
	/*check if anutu run or not*/
	while(1){
		if(check_antutu_is_running()){
			if(BOOST_DISABLED == cur_boost_status){
				/*save old status*/
				save_old_status();
				force_cpuonline_fromsys(online_mask);
				set_intvalue_fromsys(littecore_max_freq_path, little_max_freq);
				set_intvalue_fromsys(littecore_min_freq_path, little_min_freq);
				set_intvalue_fromsys(bigcore_max_freq_path, big_max_freq);
				set_intvalue_fromsys(bigcore_min_freq_path, big_min_freq);
				set_intvalue_fromsys(mif_max_freq_path, mif_max_freq);
				set_intvalue_fromsys(mif_min_freq_path, mif_min_freq);
				set_intvalue_fromsys(int_max_freq_path, int_max_freq);
				set_intvalue_fromsys(int_min_freq_path, int_min_freq);
				set_intvalue_fromsys(disp_max_freq_path, disp_max_freq);
				set_intvalue_fromsys(disp_min_freq_path, disp_min_freq);
				if(threeD_freq_value){
					set_intvalue_fromsys(threeD_dvfs_enable_path, 0);
					set_intvalue_fromsys(threeD_freq_path, threeD_freq_value);
				}
				if(tmu_disabled){
					write_sys_file(tmu_disable_path,"disabled", 8);	
				}
				set_intvalue_fromsys(hmp_boost_enable_path, hmp_boost?1:0);				
			}	
			cur_boost_status = BOOST_ENABLED;
		}
		else{
			if(BOOST_ENABLED == cur_boost_status){
				/*restore old status*/
				restore_old_status();
				/*re-enable tmu directly*/
				if(tmu_disabled){
					write_sys_file(tmu_disable_path,"enabled", 7);	
				}
			}	
			cur_boost_status = BOOST_DISABLED;
		}		
		sleep(POLLING_INTERVAL);
	}	
	return res;
}
