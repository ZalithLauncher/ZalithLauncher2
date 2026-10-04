//
// Created by maks on 19.06.2023.
//

#define _GNU_SOURCE // we are GNU GPLv3

#include <linux/limits.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sched.h>
#include <string.h>
#include <errno.h>

#define FREQ_MAX 256
void bigcore_format_cpu_path(char* buffer, unsigned int cpu_core) {
    snprintf(buffer, PATH_MAX, "/sys/devices/system/cpu/cpu%i/cpufreq/cpuinfo_max_freq", cpu_core);
}

void bigcore_set_affinity() {
    char path_buffer[PATH_MAX];
    char freq_buffer[FREQ_MAX];
    char* discard;
    unsigned long core_freq;
    unsigned long max_freq = 0;
    unsigned int corecnt = 0;

    unsigned long core_freqs[128];
    memset(core_freqs, 0, sizeof(core_freqs));

    while(corecnt < 128) {
        bigcore_format_cpu_path(path_buffer, corecnt);
        int corefreqfd = open(path_buffer, O_RDONLY);
        if(corefreqfd != -1) {
            ssize_t read_count = read(corefreqfd, freq_buffer, FREQ_MAX - 1);
            close(corefreqfd);
            if(read_count > 0) {
                freq_buffer[read_count] = 0;
                core_freq = strtoul(freq_buffer, &discard, 10);
                core_freqs[corecnt] = core_freq;
                if(core_freq > max_freq) {
                    max_freq = core_freq;
                }
            }
        }else{
            break;
        }
        corecnt++;
    }

    if(max_freq == 0 || corecnt == 0) {
        printf("bigcore: unable to determine CPU frequencies\n");
        return;
    }

    // Set affinity for all big and prime cores (>= 80% of maximum CPU frequency)
    // On modern big.LITTLE architectures (e.g. 1 prime + 3 big + 4 little, or 2 big + 6 little),
    // this ensures all high-performance cores are utilized rather than locking everything to core 0.
    unsigned long threshold = (max_freq * 80) / 100;
    cpu_set_t bigcore_affinity_set;
    CPU_ZERO(&bigcore_affinity_set);
    unsigned int big_cores_count = 0;

    for(unsigned int i = 0; i < corecnt; i++) {
        if(core_freqs[i] >= threshold) {
            CPU_SET_S(i, CPU_SETSIZE, &bigcore_affinity_set);
            big_cores_count++;
            printf("bigcore: included core %u with frequency %lu Hz\n", i, core_freqs[i]);
        }
    }

    printf("bigcore: max frequency %lu Hz, total big/prime cores: %u / %u\n", max_freq, big_cores_count, corecnt);
    int result = sched_setaffinity(0, CPU_SETSIZE, &bigcore_affinity_set);
    if(result != 0) {
        printf("bigcore: setting affinity failed: %s\n", strerror(errno));
    }else{
        printf("bigcore: forced process onto %u big CPU cores\n", big_cores_count);
    }
}