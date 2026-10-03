#ifdef DRMFUNC

DRMFUNC(drmModeResPtr,drmModeGetResources,(int fd))
DRMFUNC(drmModeConnectorPtr,drmModeGetConnector,(int fd, uint32_t connectorId))
DRMFUNC(void, drmModeFreeConnector,(drmModeConnectorPtr ptr))
DRMFUNC(drmModeEncoderPtr,drmModeGetEncoder,(int fd, uint32_t encoder_id))
DRMFUNC(void,drmModeFreeEncoder,(drmModeEncoderPtr ptr))
DRMFUNC(int,drmModeRmFB,(int fd, uint32_t bufferId))
DRMFUNC(int,drmModeAddFB,(int fd, uint32_t width, uint32_t height, uint8_t depth, uint8_t bpp, uint32_t pitch, uint32_t bo_handle, uint32_t *buf_id))
DRMFUNC(int,drmModeSetCrtc,(int fd, uint32_t crtcId, uint32_t bufferId, uint32_t x, uint32_t y, uint32_t *connectors, int count, drmModeModeInfoPtr mode))

#endif