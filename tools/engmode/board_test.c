#include <stdlib.h>
#include "ui.h"
#include "test.h"
#include "event.h"
#include "process.h"
#include "event_queue.h"
#include "i2c-dev.h"
#include <string.h>
#include "console.h"
#include "bluetooth.h"
#include <linux/videodev2.h>
//#include <linux/videodev2_samsung.h>

#define BMP18X_CHIP_ID_REG		0xD0
#define BMP18X_CHIP_ID			0x55

#define MPU6500_ID               0x74      /* unique WHOAMI */

#define MPU6500_ID_REG           0x75

static struct win board_test_win =
{
	.title = "Board Test",
	.cur = 0,
	.n = 0,
};

/* sdcard test function */
int sd_test(void)
{
	int fd;
	int ret = FAIL;
	char rbuf[10];

	system("mount -t vfat /dev/block/mmcblk0p1 /sdcard");
	usleep(500*1000);

	fd = open("/sdcard/test_sdcard.txt", O_RDWR|O_CREAT, 00664);
	if(fd < 0)
		ret = FAIL;
	else {
		write(fd, "OK", 2);
		lseek(fd, 0, SEEK_SET);
		read(fd, rbuf, 10);
		if(strncmp(rbuf, "OK", 2) == 0)
			ret = OK;
	}

	system("rm -f /sdcard/test_sdcard.txt");

	return ret;
}
int handle_null(void)
{
	int ret;
	
	pr_info("Enter %s\n", __func__);

	// TODO
	// ... ...

	// wait input event
	// ret: OK -> KEY_END, Redo -> KEY_REDO, Fail -> KEY_BACK 

	// OK: 0, Fail: -1
	return FAIL;
}

int handle_sim_test(void)
{
	int ret = -1;
#if 0
	pr_info("Enter %s\n", __func__);

	int modem_fd=-1;
	char rsp[RSPLEN] = {0};

	int i;

	if (open_tty_port(&modem_fd, tty_modem, ATP_TTY_TYPE_MODEM) < 0) {
       	return ret;
        }

	for (i=0; i<3; ++i)
	{
		
		ret = send_at_cmd(modem_fd, "AT+CPIN?\r", rsp, sizeof(rsp));  //AT+CPIN?
		if(ret <= 0)
			break;
		
		sleep(1);
	}

#endif
	return ret;
}

unsigned char get_sensor_id(const char *path, unsigned char addr, unsigned char reg)
{
	int fd;
	struct i2c_smbus_ioctl_data ctrl_data;
	union i2c_smbus_data sdata;

	fd = open(path, O_RDWR);
	if (fd < 0)
	{
		pr_info("Fail to open %s\n", path);
		return -1;
	}

	if (ioctl(fd, I2C_SLAVE_FORCE, addr) < 0)
	{
		pr_info("Fail to set i2c addr 0x%02x\n", addr);
		return -1;
	}

	ctrl_data.read_write = I2C_SMBUS_READ;
	ctrl_data.command = reg;
	ctrl_data.size = I2C_SMBUS_BYTE_DATA;
	ctrl_data.data = &sdata;

	if (ioctl(fd, I2C_SMBUS, &ctrl_data) < 0)
	{
		pr_info("Fail to read chip id(%s: addr = 0x%02x)\n", path, addr);
		return -1;
	}

	close(fd);
	return ctrl_data.data->byte;
}

int psensor_test(void)
{
	int ret = -1;
	unsigned char chip_id;
	pr_info("psensor_test\n");
	chip_id = get_sensor_id("/dev/i2c-6", (0xee>>1), BMP18X_CHIP_ID_REG);
	pr_info("psensor id: 0x%02x\n", chip_id);
	if(chip_id == BMP18X_CHIP_ID)
		ret = 0;

	return ret;
}

int msensor_test(void)
{
	int ret = -1;
	unsigned char chip_id;
	pr_info("msensor_test\n");
	chip_id = get_sensor_id("/dev/i2c-6", 0x68, MPU6500_ID_REG);
	pr_info("msensor id: 0x%02x\n", chip_id);
	if(chip_id == MPU6500_ID)
		ret = 0;

	return ret;
}

int gsensor_test(void)
{
	int ret = -1;
	unsigned char chip_id;
	pr_info("gsensor_test\n");
	chip_id = get_sensor_id("/dev/i2c-6", 0x68, MPU6500_ID_REG);
	pr_info("gsensor id: 0x%02x\n", chip_id);
	if(chip_id == MPU6500_ID)
		ret = 0;

	return ret;
}

int lsensor_test(void)
{
	int ret = -1;
	unsigned char chip_id;
	pr_info("lsensor_test\n");
	chip_id = get_sensor_id("/dev/i2c-6", 0x68, MPU6500_ID_REG);
	pr_info("lsensor id: 0x%02x\n", chip_id);
	if(chip_id == MPU6500_ID)
		ret = 0;

	return ret;
}

int Gyrosensor_test(void)
{
	int ret = -1;
	unsigned char chip_id;
	pr_info("grosensor_test\n");
	chip_id = get_sensor_id("/dev/i2c-6", 0x68, MPU6500_ID_REG);
	pr_info("grosensor id: 0x%02x\n", chip_id);
	if(chip_id == MPU6500_ID)
		ret = 0;

	return ret;
}

#define BAT_PATH 	"/sys/class/power_supply/battery/batt_vol"
int battery_test(void)
{
	int fd, ret;
	int vol;
	char voltage[50];

	fd = open(BAT_PATH, O_RDONLY);
	if(fd<0)
	{
		pr_info("Fail to open %s\n", BAT_PATH);
		return -1;
	}
	ret = read(fd, voltage, 50);
	vol = atoi(voltage);
	if(vol > 10000)
	{
		vol = vol/1000;			//Battery driver has modified the unit from mV to uV
	}
	pr_info("battery voltage = %d\n", vol);
	
	close(fd);

	return 0;
}

#define CHARGER_PATH   "/sys/class/power_supply/ac/online"
int charger_ic_test(void)
{
       int fd, ret;
       int exist = 0;
       char exist_str[20];
       
       fd = open(CHARGER_PATH, O_RDONLY);
       if(fd < 0)
       {
               pr_info("Fail to open %s\n", CHARGER_PATH);
               return -1;
       }
       ret = read(fd, exist_str, 20);
       exist = atoi(exist_str);
       if(exist != 1)
       {
               pr_info("Detect Charger-IC Failed! \n");
               return -1;
       }
       pr_info("charger-IC exist = %d\n", exist);
       close(fd);

       return 0;
}

int handle_rtc_test(void)
{
       char data[1024];
       int fd, n;
       char *ret = NULL;
       
       fd = open("/proc/driver/rtc", O_RDONLY);
       if(fd < 0)
       {
               pr_info("Open rtc proc file failed! fd=0x%x\n", fd);
               return -1;
       }
       n = read(fd, data, 1023);
       close(fd);
       if(n < 0)
       {
               pr_info("read rtc proc file failed! read_return %d", n);
               return -1;
       }
       data[n] = 0;
       pr_info("read rtc-value = %s \n", data);
       ret = strstr(data, "rtc_time");
       if(!ret)
       {
               pr_info("rtc: search string failed! \n");
               return -1;
       }

       return 0;
}

int frontcamera_test(void)
{
	int ret = -1;
	int fd;
	int  index=1;
	const char *fimc0_device   = "/dev/video41";
	unsigned short readvar = 0;
        struct v4l2_input input;
	fd = open (fimc0_device, O_RDWR);				
	if (fd < 0)
	{
		pr_info("fimc0 open fail!!!\n");
		return FAIL;
	} 
	input.index = index;
        if (ioctl(fd, VIDIOC_S_INPUT, &input) < 0) {
                printf("fimc set input fail!!!");
                close(fd);//we should close fd whatever
                return FAIL;
        }

	close(fd);
	
	return OK;
}

int rearcamera_test(void)
{
	int ret = -1;
	int fd;
	int  index=1;
	const char *fimc0_device   = "/dev/video40";
	unsigned short readvar = 0;
        struct v4l2_input input;
	fd = open (fimc0_device, O_RDWR);				
	if (fd < 0)
	{
		pr_info("fimc0 open fail!!!\n");
		return FAIL;
	} 
	input.index = index;
        if (ioctl(fd, VIDIOC_S_INPUT, &input) < 0) {
                printf("fimc set input fail!!!");
                close(fd);//we should close fd whatever
                return FAIL;
        }

	close(fd);
	
	return OK;
}
/* bluetooth test function  */
int bt_test(void)
{
	int times;
	int fd;
	int ret = 0;
	unsigned char data[1024];
	/* power on bt */
	ret = system("echo 1 > /sys/bus/hydra/devices/0005/enable");
	pr_info("power on bt\n");
       if(ret < 0) {
	pr_info("power on bt fail\n");
         return FAIL;
       }
	sleep(1);
    	if ( HAL_load() < 0 ) {
        perror("HAL failed to initialize, exit\n");
  	return FAIL;
	}
     	ret = bt_enable();
        sleep(1);
       	if(ret < 0) 
       	{
		pr_info("up bt fail\n");
		ret = FAIL;
		goto out;
       	}
       else
	{
		ret = OK;
		pr_info("up bt success\n");
		goto out;
	}
out:
	pr_info("power off bt\n");
	system("echo 0 > /sys/bus/hydra/devices/0005/enable");
	bt_disable();
	HAL_unload();
    	sleep(1);

	return ret;
}

//Board test for wlan
static int wlan_insmode_status = 0;
//Remove Wlan Modules
int rmmod_wlan(void)
{
   int ret = 0;
   ret = system("ifconfig wlan0 down");
   if(ret < 0) {
     return FAIL;
    }
    pr_info("ifconfig wlan0 down \n");


    sleep(1);
    ret = system("/system/bin/rmmod /system/lib/modules/unifi_sdio.ko");
   if(ret < 0) {
    return FAIL;
   }
    pr_info("rmmod_wlan(): Done! \n");
    return 0;
}
//Insert Wlan Modules
int insmod_wlan(void)
{
  int ret = 0;
    if(wlan_insmode_status == 0) {

	ret = system("/system/bin/insmod /system/lib/modules/unifi_sdio.ko");

       if(ret < 0) {
	system("/system/bin/rmmod /system/lib/modules/unifi_sdio.ko");
         return -1;
       }

       wlan_insmode_status = 1;
    }


    return 0;
}
//try 10 times to do "ifconfig wlan0 up"
int wlan_up(void)
{
    int ret = 0, times = 0;
    
    while(times < 10) {
          pr_info("ifconfig wlan0 up: while(times) \n");

          ret = system("ifconfig wlan0 up");
          if (ret != 0) {
            sleep(1);
          }else{
            break;
          }
              
          times ++;
    }

    if(times >= 10) {
       pr_info("ifconfig wlan0 up: fail! \n");

       goto TestFail;
    }


    return 0;

TestFail:
         return -1;
}
int wifi_test(void)
{
    int wlan_err = 0, read_up_err = 0,read_err = 0;
    pr_info("wifi_test\n");
    wlan_err = insmod_wlan();

    if (wlan_err < 0) {
       goto TestFail;
    }
    read_up_err = wlan_up();
    if (read_up_err < 0) {
       goto TestFail;
    }

    pr_info("Wlan Board Test: Done \n");
    rmmod_wlan();
    return 0;
TestFail:
         rmmod_wlan();
         pr_info("Wlan Board Test: Fail! \n");
         return -1;  
	return FAIL;
}
static int audio_test(void)
{
	int times;
	int fd;
	int ret;
	unsigned char data[1024];

	/* set the spk mixer. some mixers just use dfl value */
	ret = system("tinymix 158 1");
	if (ret < 0)
		return FAIL;

	ret = system("tinymix 163 1");
	if (ret < 0)
		return FAIL;

	ret = system("tinymix 148 1");
	if (ret < 0)
		return FAIL;

	ret = system("tinymix 143 1");
	if (ret < 0)
		return FAIL;

	ret = system("tinymix 123 1");
	if (ret < 0)
		return FAIL;

	ret = system("tinymix 67 1");
	if (ret < 0)
		return FAIL;

	ret = system("tinymix 64 3");
	if (ret < 0)
		return FAIL;

	ret = system("tinymix 68 4");
	if (ret < 0)
		return FAIL;

	/* play the test music file for about 3 second */
	ret = system("tinyplay /system/media/audio/alarms/spk.wav &");
	if (ret < 0)
		return FAIL;

	return OK;
}

/**
 * shaoguodong:
 * if the test code is not ready yet,
 * make sure keep the 'test' feild as NULL
 * the test framework will know that this sub test
 * is NA yet
 */
static struct test_list works[] =
{
	{"SD:",	sd_test},
	{"SIM:", NULL},
	{"SN:", NULL},
	{"Battery:", battery_test},
	{"Audio:", audio_test},
	{"Bluetooth:", bt_test},
	{"WIFI:", wifi_test},
	{"GPS:", NULL},
	{"GSensor:", gsensor_test},
	{"MSensor:", msensor_test},
	{"PSensor:", psensor_test},
	{"LSensor:", lsensor_test},
	{"RTC:", handle_rtc_test},
	{"FCamera:", frontcamera_test},
	{"RCamera:", rearcamera_test},
	{"Gyro:", Gyrosensor_test},
	{"Charger:", charger_ic_test},
};

static void proc_event()
{
	struct event event;
	while (1) {
		dequeue_event_locked(&event);
		if (event.type == KEY_POWER ||
				event.type == HOST_EV_ENTER)
			return;
	}
}

void board_test_win_work(void)
{
	int i;
	int row = 2;
	char to_host[1000];
	char* p;
	int n;
	char* head = "engmode: ";//"*#rst#*";

	p = to_host;
	strcpy(p, head);
	n = strlen(head);
	p+=n;

	draw_win(&board_test_win);
	for (i = 0; i < sizeof(works)/sizeof(struct test_list); i++) {
		ui_puts(works[i].name, row, 1, font_color);
		strcpy(p, works[i].name);
		n = strlen(works[i].name);
		p[n-1] = ' ';
		p += n;

		if (works[i].test != NULL) {
			if (works[i].test() == OK) {
				ui_puts_right("OK", row, ok_color);
				strcpy(p, "OK");
			}
			else {
				ui_puts_right("FAIL", row, fail_color);
				strcpy(p, "FAIL");
			}
		} else {
			ui_puts_right("N/A", row, na_color);
			strcpy(p, "N/A");
		}
		row++;

		print("%s\n", p-n);
	}
	print("*#state#*test finished\n");
	proc_event();
	return;
}
