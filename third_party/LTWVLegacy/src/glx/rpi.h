#ifndef _GLX_RPI_H_
#define _GLX_RPI_H_

// Code specific to RPI

void rpi_init();
void rpi_fini();
void* create_rpi_window(int w, int h);
void delete_rpi_window(void* win);

#endif // _GLX_RPI_H_
