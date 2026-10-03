#ifdef GBMFUNC

GBMFUNC(int,gbm_device_get_fd,(struct gbm_device *gbm))
GBMFUNC(int,gbm_device_is_format_supported,(struct gbm_device *gbm,
                                                   uint32_t format, uint32_t usage))
GBMFUNC(void,gbm_device_destroy,(struct gbm_device *gbm))
GBMFUNC(struct gbm_device *,gbm_create_device,(int fd))
GBMFUNC(unsigned int,gbm_bo_get_width,(struct gbm_bo *bo))
GBMFUNC(unsigned int,gbm_bo_get_height,(struct gbm_bo *bo))
GBMFUNC(uint32_t,gbm_bo_get_stride,(struct gbm_bo *bo))
GBMFUNC(union gbm_bo_handle,gbm_bo_get_handle,(struct gbm_bo *bo))
GBMFUNC(int,gbm_bo_write,(struct gbm_bo *bo, const void *buf, size_t count))
GBMFUNC(struct gbm_device *,gbm_bo_get_device,(struct gbm_bo *bo))
GBMFUNC(void,gbm_bo_set_user_data,(struct gbm_bo *bo, void *data,
                                          void (*destroy_user_data)(struct gbm_bo *, void *)))
GBMFUNC(void *,gbm_bo_get_user_data,(struct gbm_bo *bo))
GBMFUNC(void,gbm_bo_destroy,(struct gbm_bo *bo))
GBMFUNC(struct gbm_bo *,gbm_bo_create,(struct gbm_device *gbm,
                                              uint32_t width, uint32_t height,
                                              uint32_t format, uint32_t usage))
GBMFUNC(struct gbm_surface *,gbm_surface_create,(struct gbm_device *gbm,
                                                        uint32_t width, uint32_t height,
                                                        uint32_t format, uint32_t flags))
GBMFUNC(void,gbm_surface_destroy,(struct gbm_surface *surf))
GBMFUNC(struct gbm_bo *,gbm_surface_lock_front_buffer,(struct gbm_surface *surf))
GBMFUNC(void,gbm_surface_release_buffer,(struct gbm_surface *surf, struct gbm_bo *bo))

#endif