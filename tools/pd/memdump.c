#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <fcntl.h>
#include <ctype.h>
#include <termios.h>

#include <sys/mman.h>


#define DRAM_START_ADDR 0x40000000
#define DRAM_END_ADDR 0xff700000
#define DEV_MEM		"/dev/mem"
#define DEV_KMEM	"/dev/kmem"
#define MAP_SIZE	0x1000    //4K page align
#define MAP_MASK	(MAP_SIZE-1)

#define ARRAY_SIZE(arr) (sizeof (arr) / sizeof ((arr)[0]))
#define FATAL do { fprintf(stderr, "Error at line %d, file %s (%d) [%s]\n", \
  __LINE__, __FILE__, errno, strerror(errno)); exit(1); } while(0)
  

typedef  unsigned int u32;
typedef  unsigned long uAddr; 
typedef struct cmd_table_s {
	char 		*name;				/* Command Name */
	int			(*func)(int, char *[]);
}cmd_table_s;

struct sfr_region {
    char region_name[10];
    uAddr pa_start;
    uAddr pa_end;
};

typedef enum SFR_TYPE {
	SFR_NORMAL,
	SMC_ZONE,
	SFR_INVALID,
}SFR_TYPE;

struct sfr_region exynos_sfr_regions[] = {
    {
        .region_name = "SFR",
        .pa_start = 0x10000000,
        .pa_end = 0x160A0000,        
    },
    {
	.region_name = "DRAM",
        .pa_start = DRAM_START_ADDR,
        .pa_end = DRAM_END_ADDR, 
    },
};

struct sfr_region exynos_smc_regions[] = {
    {
        .region_name = "SMC",
        .pa_start = 0x04000000,
        .pa_end = 0x08000000,
    },
};
static SFR_TYPE check_dump_region(uAddr reg_base, u32 offset )
{
	int i;
	int sfr_type = SFR_INVALID;

	if( reg_base & 0x3)
		return SFR_INVALID;
	for(i = 0; i < ARRAY_SIZE(exynos_smc_regions); i++) {
		if(reg_base >= exynos_smc_regions[i].pa_start && (reg_base + offset) < exynos_smc_regions[i].pa_end) {
			return SMC_ZONE;
		}
	}
	for(i = 0; i < ARRAY_SIZE(exynos_sfr_regions); i++) {
		if(reg_base >= exynos_sfr_regions[i].pa_start && (reg_base + offset) < exynos_sfr_regions[i].pa_end) { 
			return SFR_NORMAL;
		}
	}
	return SFR_INVALID;
}

static u32 phy_read_reg32(uAddr u32Addr)
{
	void *map_base, *virt_addr;
	u32 u32Read;
	int fd;

	if((fd = open(DEV_MEM, O_RDWR | O_SYNC)) == -1) {
		printf("[ERR] Can't open %s\n", DEV_MEM);
		return -1;
	}
		
	// physical base address, 4K page align, minimal mapping size is 4K
	map_base = mmap(0, MAP_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, (u32Addr & ~MAP_MASK));
	if(map_base == (void *) -1) {
            FATAL;
       }
	virt_addr = (void *)((char *)map_base + (u32Addr & MAP_MASK));
	u32Read = *((volatile u32 *) virt_addr);
	if(munmap(map_base, MAP_SIZE) == -1) {
            FATAL;
       }
	close(fd);
	
	printf("\nGetting PAddr: %p : 0x%08x\n", u32Addr, u32Read);
	
	return u32Read;
}

static u32 phy_write_reg32(uAddr u32Addr, u32 u32Val)
    {
        void *map_base, *virt_addr; 
        u32 read_back;
        int fd;
        
        if((fd = open(DEV_MEM, O_RDWR | O_SYNC)) == -1) {
            printf("[ERR] Can't open %s\n", DEV_MEM);
            return -1;
        }
        // physical base address, 4K page align
        map_base = mmap((void*)0, MAP_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd,  (u32Addr & ~MAP_MASK) );
        if(map_base == (void *) -1) {
            FATAL;
        }
    
        virt_addr = (void *)((char *)map_base + (u32Addr & MAP_MASK));
        *((volatile u32 *) virt_addr) = u32Val;
        
        read_back = *((volatile u32 *) virt_addr);
        
        if(munmap(map_base, MAP_SIZE) == -1) {
            FATAL;
        }
        close(fd);
        printf("\nSetting PAddr: %p : 0x%08x\n         Read: %p : 0x%08x\n", u32Addr,u32Val,u32Addr, read_back);
        return read_back;
    }

static int phy_dump_reg32(uAddr u32Addr, u32 size)
{
    void *map_base, *virt_addr;
    u32 map_size,  offset, i, count = 0;
    uAddr map_addr;
    int fd, pagecount;
    
    if((fd = open(DEV_MEM, O_RDWR | O_SYNC)) == -1) {
        printf("[ERR] Can't open %s\n", DEV_MEM);
        return -1;
    }
    
    offset = size & ~0x3;  //32bit align;
    map_addr = (u32Addr & ~MAP_MASK);
    pagecount = (((u32Addr + offset) % MAP_SIZE)?((u32Addr + offset) / MAP_SIZE + 1):(u32Addr + offset) / MAP_SIZE);
    map_size = (pagecount - map_addr / MAP_SIZE) * MAP_SIZE;

// physical base address, 4K page align, minimal mapping size is 4K
    map_base = mmap(0, map_size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, map_addr);
    if(map_base == (void *) -1) {
        FATAL;
    }

    printf("\nSFR MEM DUMP, Base : 0x%p, Size :0x%08x\n",u32Addr, offset);

    virt_addr = (void *)((char *)map_base + (u32Addr & MAP_MASK));
    
    if(offset < 0x10) {
        printf("%p : ",u32Addr);
        for(i = 0; i < offset / 4; i++) {
            printf("%x ", *((volatile u32*)((char *)virt_addr + i * 4)));
        }
        printf("\n");
        goto OK;
    }    
    for(i = 0; i< offset>>4; i++) {
        printf("%p : %x %x %x %x \n", u32Addr + count, *((volatile u32*)(char *)virt_addr), 
            *((volatile u32*)((char *)virt_addr + 4)), *((volatile u32*)((char *)virt_addr + 8)), *((volatile u32*)((char *)virt_addr + 12)));
        virt_addr = (void *)((char *)virt_addr + 0x10);
        count += 0x10;
    }
    if( count < offset ) {
        printf("%p : ", u32Addr + count );
        for(;count < offset ; count+=0x4)
            printf("%x ",*((volatile u32*)((char *)virt_addr + count)));
        printf("\n");
    }
    printf("\n");

OK:
    if(munmap(map_base, map_size) == -1) {
        FATAL;
    }
    close(fd);
    return 0;
}

void kmem_dump_func(uAddr u32Vaddr, u32 size)
{
    int fd, pagecount;;
    void *mbase, *ptr;
    uAddr regAddr, varAddr;
    u32 map_size, count, offset, i;

    offset = size & ~0x3;  //32bit align;

    if(u32Vaddr < 0xFFFFFFc000000000 || (u32Vaddr + offset) > 0xFFFFFFc0c0000000||/**/ (u32Vaddr & 0x3))
    {
        printf("\n[EEROR], KMEM dump out of region,  0xFFFFFFc000000000 ~ 0xFFFFFFc0c0000000  addr should align to 4\n");
        return;
    }

    varAddr = (u32Vaddr & ~MAP_MASK);
    pagecount = (((u32Vaddr + offset) % MAP_SIZE)?((u32Vaddr + offset) / MAP_SIZE + 1):((u32Vaddr + offset) / MAP_SIZE));
    map_size = (pagecount - varAddr / MAP_SIZE) * MAP_SIZE;

    fd = open(DEV_KMEM, O_RDONLY);
    if (fd == -1) {
        printf("open %s failure\n", DEV_KMEM);
        return;
    }

    mbase = mmap(0,map_size,PROT_READ,MAP_SHARED, fd, varAddr);
    if(mbase == (void *) -1) {
        printf("map failed %s,map_size=0x%x, addr=%p\n", strerror(errno),map_size, varAddr);
    }

    printf("\nKMEM DUMP, Base : %p, Size :0x%x\n",u32Vaddr, offset);

    ptr = (void *)((char *)mbase + (u32Vaddr & MAP_MASK));
    count  = 0;
    if(offset < 0x10) {
        printf("%p : ",u32Vaddr);
        for(i = 0; i < offset / 4; i++) {
            printf("%x ", *((volatile u32*)((char *)ptr + i * 4)));
        }
        printf("\n");
        goto OK;
    }    
    for(i = 0; i< offset>>4; i++) {
         printf("%p : %x %x %x %x \n", u32Vaddr + count, *((volatile u32*)((char *)ptr + count) ), 
              *((volatile u32*)((char *)ptr + count + 4)), *((volatile u32*)((char *)ptr + count + 8)), *((volatile u32*)((char *)ptr + count + 12)));
         count  += 0x10;
         ptr = (void *)((char *)ptr + 0x10);
    }
    if( count < offset ) {
        printf("%p : ", u32Vaddr + count );
        for(;count < offset ; count+=0x4)
            printf("%x ",*((volatile u32*)((char *)ptr + count)));
        printf("\n");
    }
    printf("\n");

OK:
    close(fd);
    munmap(mbase,map_size);
    return;
}


static int get_sfr_func(int argc, char **argv)
{
	u32 paddr;
       SFR_TYPE sfr_type = SFR_INVALID;
	if(argc != 1 ) {
		printf("\nParameter error\n"
				"\tExample:\n"
				"\tpd gr 0x10010000  \t: Get SFR 0x10010000 value\n");
		return -1;
	}
	
	paddr = (u32)strtoul(argv[0], NULL, 0);
       sfr_type = check_dump_region(paddr ,0);
       switch(sfr_type) {
            case SFR_NORMAL:
            case SMC_ZONE:
                phy_read_reg32(paddr);
                break;
             case SFR_INVALID:
             default:
                printf("Input Paddr is invalid address to read %p\n", paddr);
                break;
        }
	return 0;
}

static int set_sfr_func(int argc, char **argv)
{
	u32 paddr, val;
       SFR_TYPE sfr_type = SFR_INVALID;
	if(argc != 2) {
		printf("\nParameter error\n"
				"\tExample:\n"
				"\tpd sr 0x10010000  0xff\t: Set SFR 0x10010000 value 0xff\n");
		return -1;
	}
	paddr = (u32)strtoul(argv[0], 0, 0);
	val = (u32)strtoul(argv[1], 0, 0);
       sfr_type = check_dump_region(paddr ,0);
       switch(sfr_type) {
           case SFR_NORMAL: 
           case SMC_ZONE: 
               phy_write_reg32(paddr, val);
               break;
           case SFR_INVALID:
           default:
               printf("Input Paddr is invalid address to write %p\n", paddr);
               break;
           }
	return 0;
}

static int dump_sfr_func(int argc, char **argv)
{
	u32 paddr, offset;
	SFR_TYPE sfr_type = SFR_INVALID;
	if(argc < 1 || argc > 2) {
		printf("\nParameter error\n"
				"\tExample:\n"
				"\tpd dr 0x10010000  0x10\t: Dump SFR Base : 0x10010000, Offset : 0x10\n");
		return -1;
	}
	
	paddr = (u32)strtoul(argv[0], 0, 0);
	if(argc == 2)
		offset = (u32)strtoul(argv[1], 0, 0);
	else
		offset = 0x40;
	sfr_type = check_dump_region(paddr ,offset);
	switch(sfr_type) {
		case SFR_NORMAL: 
            case SMC_ZONE: 
			phy_dump_reg32(paddr, offset);
			break;
		case SFR_INVALID:
		default:
			printf("Input Paddr is invalid region to dump base: %p, offset : 0x%x \n", paddr, offset);
			break;
		}
	return 0;
}

static int dump_kmem_func(int argc, char **argv)
{
	uAddr paddr;
	u32 offset;

	if(argc < 1 || argc > 2) { 
		printf("\nParameter error\n"
				"\tExample:\n"
				"\tpd dk 0xFFFFFFc000000000  0x10\t: Dump SFR Base : 0xFFFFFFc000000000, Offset : 0x10\n");
		return -1;
	}
	paddr = (uAddr)strtoul(argv[0], 0, 0);
	if(argc == 2)
		offset = (u32)strtoul(argv[1], 0, 0);
	else
		offset = 0x40;
	kmem_dump_func(paddr, offset);
	return 0;
}

cmd_table_s *find_cmd_table(const char *cmd, cmd_table_s *table, int table_len)
{
	cmd_table_s * tp;
	cmd_table_s * cmd_tmp = table;
	unsigned int len;

	if(!cmd)
		return NULL;

	len = strlen(cmd);
	for(tp = table;  table_len -1 >= tp - table; tp++) {
		if((!strncmp(cmd, tp->name, len)) && (len == strlen(tp->name))) {
			return tp;
		}
	}
	return NULL;
}

cmd_table_s mem_cmd [] = {
		{"gr",	get_sfr_func},
		{"sr",	set_sfr_func},
		{"dr",	dump_sfr_func},
		{"dk",	dump_kmem_func},
              {"end",   NULL}      
};

void print_help()
{
	printf("\nUsage: pd {op} { address } [ data ]  \n"
			"\top\t: gr, sr, dr, dk\n"
			"\taddress\t: memory address to act upon\n"
			"\tdata\t: data to be written\n");
	printf("\nExample:\n"
			"\tpd gr 0x10010000  \t: Get SFR 0x10010000 value\n"
			"\tpd sr 0x10010000 0xFF\t: Set SFR 0x10010000 value 0xFF\n"
			"\tpd dr 0x10010000 0x10\t: Dump SFR, base: 0x10010000, offset:0x10\n"
			"\tpd dk 0xFFFFFFc000000000 0x100\t: Dump KMEM, base:0xFFFFFFc000000000, offset:0x100\n"
		);
}

int main(int argc, char **argv )
{
	cmd_table_s * cmd;

	if(argc < 2) {
		print_help();
		exit(0);
	}
	
	argc--;
	argv++;
	
	if( (cmd = find_cmd_table(argv[0], mem_cmd, ARRAY_SIZE(mem_cmd))) == NULL) {
		printf("\n  [ERR] unsupport cmd\n");
		print_help();
		exit(0);
	}

	argc--;
	argv++;
	
	cmd->func(argc , argv);

	return 0;
}
