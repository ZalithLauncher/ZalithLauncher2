//
// Created by movte on 2026/5/13.
//

#include <jni.h>
#include <dlfcn.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include <vulkan/vulkan.h>


static jobject g_logCallback = NULL;
static jmethodID g_logMethod = NULL;

static void vulkan_log(JNIEnv *env, const char *level, const char *fmt, ...) {
    if (!g_logCallback || !g_logMethod || !env) return;

    char buffer[1024];
    va_list args;
    va_start(args, fmt);
    vsnprintf(buffer, sizeof(buffer), fmt, args);
    va_end(args);

    jstring jLevel = (*env)->NewStringUTF(env, level);
    jstring jMsg   = (*env)->NewStringUTF(env, buffer);

    (*env)->CallVoidMethod(env, g_logCallback, g_logMethod, jLevel, jMsg);

    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionClear(env);
    }

    (*env)->DeleteLocalRef(env, jLevel);
    (*env)->DeleteLocalRef(env, jMsg);
}

#define LOG_I(...) vulkan_log(env, "INFO", __VA_ARGS__)
#define LOG_W(...) vulkan_log(env, "WARN", __VA_ARGS__)
#define LOG_E(...) vulkan_log(env, "ERROR", __VA_ARGS__)

JNIEXPORT void JNICALL
Java_com_movtery_zalithlauncher_utils_device_VulkanChecker_nativeSetLogCallback(
        JNIEnv *env,
        jclass clazz,
        jobject callback
) {
    (void) clazz;

    if (g_logCallback != NULL) {
        (*env)->DeleteGlobalRef(env, g_logCallback);
        g_logCallback = NULL;
        g_logMethod = NULL;
    }

    if (callback != NULL) {
        g_logCallback = (*env)->NewGlobalRef(env, callback);
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        g_logMethod = (*env)->GetMethodID(env, cbClass, "log", "(Ljava/lang/String;Ljava/lang/String;)V");
        (*env)->DeleteLocalRef(env, cbClass);
    }
}

#define LOAD_VK_FUNC(name) PFN_##name p##name = (PFN_##name)dlsym(vulkan_handle, #name)

void *loadTurnipVulkan(const char *driver_path, const char *native_dir, const char *cache_dir);

JNIEXPORT jobject JNICALL
Java_com_movtery_zalithlauncher_utils_device_VulkanChecker_nativeCheckVulkan(
        JNIEnv *env,
        jclass clazz,
        jstring jDriverPath,
        jstring jNativeDir,
        jstring jCacheDir
) {
    (void) clazz;

    const char *driverPath = jDriverPath ? (*env)->GetStringUTFChars(env, jDriverPath, NULL) : NULL;
    const char *nativeDir  = jNativeDir  ? (*env)->GetStringUTFChars(env, jNativeDir, NULL) : NULL;
    const char *cacheDir   = jCacheDir   ? (*env)->GetStringUTFChars(env, jCacheDir, NULL) : NULL;

    void *vulkan_handle = NULL;
    if (nativeDir && cacheDir) {
#ifdef ADRENO_POSSIBLE
        vulkan_handle = loadTurnipVulkan(driverPath, nativeDir, cacheDir);
#endif
    }
    if (!vulkan_handle) {
        vulkan_handle = dlopen("libvulkan.so", RTLD_NOW | RTLD_LOCAL);
    }

    if (driverPath) (*env)->ReleaseStringUTFChars(env, jDriverPath, driverPath);
    if (nativeDir)  (*env)->ReleaseStringUTFChars(env, jNativeDir, nativeDir);
    if (cacheDir)   (*env)->ReleaseStringUTFChars(env, jCacheDir, cacheDir);

    if (!vulkan_handle) {
        LOG_E("Failed to load Vulkan library.");
        return NULL;
    }

    LOAD_VK_FUNC(vkEnumerateInstanceVersion);
    LOAD_VK_FUNC(vkCreateInstance);
    LOAD_VK_FUNC(vkDestroyInstance);
    LOAD_VK_FUNC(vkEnumeratePhysicalDevices);
    LOAD_VK_FUNC(vkGetPhysicalDeviceFeatures);
    LOAD_VK_FUNC(vkEnumerateDeviceExtensionProperties);
    LOAD_VK_FUNC(vkGetPhysicalDeviceFeatures2);
    LOAD_VK_FUNC(vkGetPhysicalDeviceProperties);
    LOAD_VK_FUNC(vkGetPhysicalDeviceProperties2);

    // 若驱动仅暴露 KHR 别名，尝试二次加载
    if (!pvkGetPhysicalDeviceFeatures2) {
        pvkGetPhysicalDeviceFeatures2 = (PFN_vkGetPhysicalDeviceFeatures2)
                dlsym(vulkan_handle, "vkGetPhysicalDeviceFeatures2KHR");
    }
    if (!pvkGetPhysicalDeviceProperties2) {
        pvkGetPhysicalDeviceProperties2 = (PFN_vkGetPhysicalDeviceProperties2)
                dlsym(vulkan_handle, "vkGetPhysicalDeviceProperties2KHR");
    }

    if (!pvkCreateInstance || !pvkDestroyInstance || !pvkEnumeratePhysicalDevices ||
        !pvkEnumerateDeviceExtensionProperties || !pvkGetPhysicalDeviceProperties) {
        LOG_E("Essential Vulkan functions missing, aborting.");
        dlclose(vulkan_handle);
        return NULL;
    }

    // 查询实例版本
    uint32_t instanceApiVersion = VK_API_VERSION_1_0;
    if (pvkEnumerateInstanceVersion) {
        if (pvkEnumerateInstanceVersion(&instanceApiVersion) == VK_SUCCESS) {
            LOG_I("Instance reports Vulkan %u.%u.%u",
                  (unsigned int) VK_API_VERSION_MAJOR(instanceApiVersion),
                  (unsigned int) VK_API_VERSION_MINOR(instanceApiVersion),
                  (unsigned int) VK_API_VERSION_PATCH(instanceApiVersion));
        } else {
            LOG_W("vkEnumerateInstanceVersion failed, fallback to 1.0");
        }
    } else {
        LOG_I("vkEnumerateInstanceVersion unavailable, assume Vulkan 1.0");
    }

    // 创建 VkInstance
    VkApplicationInfo appInfo = {
            .sType = VK_STRUCTURE_TYPE_APPLICATION_INFO,
            .pApplicationName = "ZalithLauncher",
            .applicationVersion = VK_MAKE_VERSION(1, 0, 0),
            .apiVersion = instanceApiVersion
    };
    VkInstanceCreateInfo createInfo = {
            .sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
            .pApplicationInfo = &appInfo
    };

    VkInstance instance = VK_NULL_HANDLE;
    VkResult vkRes = pvkCreateInstance(&createInfo, NULL, &instance);
    if (vkRes != VK_SUCCESS) {
        LOG_E("vkCreateInstance failed, result=%d", vkRes);
        dlclose(vulkan_handle);
        return NULL;
    }

    // 枚举物理设备
    uint32_t deviceCount = 0;
    vkRes = pvkEnumeratePhysicalDevices(instance, &deviceCount, NULL);
    if (vkRes != VK_SUCCESS || deviceCount == 0) {
        LOG_E("No Vulkan physical device found (result=%d, count=%u)", vkRes, deviceCount);
        pvkDestroyInstance(instance, NULL);
        dlclose(vulkan_handle);
        return NULL;
    }

    VkPhysicalDevice *devices = (VkPhysicalDevice *) malloc(sizeof(VkPhysicalDevice) * deviceCount);
    if (!devices) {
        LOG_E("malloc failed for device array");
        pvkDestroyInstance(instance, NULL);
        dlclose(vulkan_handle);
        return NULL;
    }

    vkRes = pvkEnumeratePhysicalDevices(instance, &deviceCount, devices);
    if (vkRes != VK_SUCCESS) {
        LOG_E("vkEnumeratePhysicalDevices (second pass) failed, result=%d", vkRes);
        free(devices);
        pvkDestroyInstance(instance, NULL);
        dlclose(vulkan_handle);
        return NULL;
    }

    /* 为简化实现，仅检测第一个物理设备。
     * Android 设备通常只有一个 GPU，若有多个（如某些平板/笔记本），
     * 取第一个即可满足启动器层面的兼容性判断。 */
    VkPhysicalDevice physicalDevice = devices[0];
    free(devices);
    LOG_I("Found %u physical device(s), inspecting first one.", deviceCount);

    // 查询设备版本
    uint32_t deviceApiVersion = VK_API_VERSION_1_0;

    // 优先使用 vkGetPhysicalDeviceProperties2 获取设备属性
    if (pvkGetPhysicalDeviceProperties2) {
        VkPhysicalDeviceVulkan11Properties vk11Props = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_1_PROPERTIES,
                .pNext = NULL
        };
        VkPhysicalDeviceProperties2 deviceProps2 = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2,
                .pNext = &vk11Props
        };

        pvkGetPhysicalDeviceProperties2(physicalDevice, &deviceProps2);
        deviceApiVersion = deviceProps2.properties.apiVersion;

        LOG_I("Device (via vkGetPhysicalDeviceProperties2) reports Vulkan %u.%u.%u",
              (unsigned int) VK_API_VERSION_MAJOR(deviceApiVersion),
              (unsigned int) VK_API_VERSION_MINOR(deviceApiVersion),
              (unsigned int) VK_API_VERSION_PATCH(deviceApiVersion));
    } else {
        // 降级到 v1.0 的 vkGetPhysicalDeviceProperties
        VkPhysicalDeviceProperties deviceProps;
        pvkGetPhysicalDeviceProperties(physicalDevice, &deviceProps);
        deviceApiVersion = deviceProps.apiVersion;

        LOG_I("Device (via vkGetPhysicalDeviceProperties) reports Vulkan %u.%u.%u",
              (unsigned int) VK_API_VERSION_MAJOR(deviceApiVersion),
              (unsigned int) VK_API_VERSION_MINOR(deviceApiVersion),
              (unsigned int) VK_API_VERSION_PATCH(deviceApiVersion));
    }

    // 枚举设备扩展
    uint32_t extCount = 0;
    vkRes = pvkEnumerateDeviceExtensionProperties(physicalDevice, NULL, &extCount, NULL);
    if (vkRes != VK_SUCCESS) {
        LOG_E("vkEnumerateDeviceExtensionProperties (count) failed, result=%d", vkRes);
        pvkDestroyInstance(instance, NULL);
        dlclose(vulkan_handle);
        return NULL;
    }

    VkExtensionProperties *exts = (VkExtensionProperties *) malloc(sizeof(VkExtensionProperties) * extCount);
    if (!exts) {
        LOG_E("malloc failed for extension array");
        pvkDestroyInstance(instance, NULL);
        dlclose(vulkan_handle);
        return NULL;
    }

    vkRes = pvkEnumerateDeviceExtensionProperties(physicalDevice, NULL, &extCount, exts);
    if (vkRes != VK_SUCCESS) {
        LOG_E("vkEnumerateDeviceExtensionProperties (data) failed, result=%d", vkRes);
        free(exts);
        pvkDestroyInstance(instance, NULL);
        dlclose(vulkan_handle);
        return NULL;
    }

    jclass listClass        = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID listInit      = (*env)->GetMethodID(env, listClass, "<init>", "()V");
    jmethodID listAdd       = (*env)->GetMethodID(env, listClass, "add", "(Ljava/lang/Object;)Z");
    jobject extensionsList  = (*env)->NewObject(env, listClass, listInit);

    for (uint32_t i = 0; i < extCount; i++) {
        jstring extName = (*env)->NewStringUTF(env, exts[i].extensionName);
        (*env)->CallBooleanMethod(env, extensionsList, listAdd, extName);
        (*env)->DeleteLocalRef(env, extName);
    }
    free(exts);
    LOG_I("Enumerated %u device extensions.", extCount);

    // 查询设备特性
    jclass mapClass     = (*env)->FindClass(env, "java/util/HashMap");
    jmethodID mapInit   = (*env)->GetMethodID(env, mapClass, "<init>", "()V");
    jmethodID mapPut    = (*env)->GetMethodID(env, mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    jobject featuresMap = (*env)->NewObject(env, mapClass, mapInit);

    jclass boolClass    = (*env)->FindClass(env, "java/lang/Boolean");
    jmethodID boolValueOf = (*env)->GetStaticMethodID(env, boolClass, "valueOf", "(Z)Ljava/lang/Boolean;");

    VkBool32 multiDrawIndirect                = VK_FALSE;
    VkBool32 fillModeNonSolid                 = VK_FALSE;
    VkBool32 samplerAnisotropy                = VK_FALSE;
    VkBool32 shaderDrawParameters             = VK_FALSE;
    VkBool32 timelineSemaphore                = VK_FALSE;
    VkBool32 hostQueryReset                   = VK_FALSE;
    VkBool32 synchronization2                 = VK_FALSE;
    VkBool32 dynamicRendering                 = VK_FALSE;
    VkBool32 vertexAttributeInstanceRateDivisor = VK_FALSE;

    if (pvkGetPhysicalDeviceFeatures2) {
        VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT featDiv = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_FEATURES_EXT
        };
        VkPhysicalDeviceDynamicRenderingFeaturesKHR featDyn = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DYNAMIC_RENDERING_FEATURES_KHR,
                .pNext = &featDiv
        };
        VkPhysicalDeviceSynchronization2FeaturesKHR featSync2 = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SYNCHRONIZATION_2_FEATURES_KHR,
                .pNext = &featDyn
        };
        VkPhysicalDeviceHostQueryResetFeatures featHostQuery = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_HOST_QUERY_RESET_FEATURES,
                .pNext = &featSync2
        };
        VkPhysicalDeviceTimelineSemaphoreFeatures featTimeline = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_TIMELINE_SEMAPHORE_FEATURES,
                .pNext = &featHostQuery
        };
        VkPhysicalDeviceShaderDrawParametersFeatures featShaderDraw = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_DRAW_PARAMETERS_FEATURES,
                .pNext = &featTimeline
        };
        VkPhysicalDeviceFeatures2 feat2 = {
                .sType = VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2,
                .pNext = &featShaderDraw
        };

        pvkGetPhysicalDeviceFeatures2(physicalDevice, &feat2);

        multiDrawIndirect     = feat2.features.multiDrawIndirect;
        fillModeNonSolid      = feat2.features.fillModeNonSolid;
        samplerAnisotropy     = feat2.features.samplerAnisotropy;
        shaderDrawParameters  = featShaderDraw.shaderDrawParameters;
        timelineSemaphore     = featTimeline.timelineSemaphore;
        hostQueryReset        = featHostQuery.hostQueryReset;
        synchronization2      = featSync2.synchronization2;
        dynamicRendering      = featDyn.dynamicRendering;
        vertexAttributeInstanceRateDivisor = featDiv.vertexAttributeInstanceRateDivisor;

        LOG_I("Queried features via vkGetPhysicalDeviceFeatures2");
    } else if (pvkGetPhysicalDeviceFeatures) {
        // Vulkan 1.0 降级路径：仅基础特性
        VkPhysicalDeviceFeatures feat;
        pvkGetPhysicalDeviceFeatures(physicalDevice, &feat);
        multiDrawIndirect  = feat.multiDrawIndirect;
        fillModeNonSolid   = feat.fillModeNonSolid;
        samplerAnisotropy  = feat.samplerAnisotropy;
        LOG_W("vkGetPhysicalDeviceFeatures2 unavailable; only basic features queried.");
    } else {
        LOG_E("Neither vkGetPhysicalDeviceFeatures2 nor vkGetPhysicalDeviceFeatures available.");
    }

#define PUT_FEAT(key, val) do { \
        jstring _k = (*env)->NewStringUTF(env, key); \
        jobject _v = (*env)->CallStaticObjectMethod(env, boolClass, boolValueOf, (jboolean)(val)); \
        (*env)->CallObjectMethod(env, featuresMap, mapPut, _k, _v); \
        (*env)->DeleteLocalRef(env, _k); \
        (*env)->DeleteLocalRef(env, _v); \
    } while (0)

    PUT_FEAT("multiDrawIndirect",                multiDrawIndirect);
    PUT_FEAT("fillModeNonSolid",                 fillModeNonSolid);
    PUT_FEAT("samplerAnisotropy",                samplerAnisotropy);
    PUT_FEAT("shaderDrawParameters",             shaderDrawParameters);
    PUT_FEAT("timelineSemaphore",                timelineSemaphore);
    PUT_FEAT("hostQueryReset",                   hostQueryReset);
    PUT_FEAT("synchronization2",                 synchronization2);
    PUT_FEAT("dynamicRendering",                 dynamicRendering);
    PUT_FEAT("vertexAttributeInstanceRateDivisor", vertexAttributeInstanceRateDivisor);
#undef PUT_FEAT

    jclass capClass = (*env)->FindClass(env, "com/movtery/zalithlauncher/utils/device/VulkanCapabilities");
    jmethodID capInit = (*env)->GetMethodID(env, capClass, "<init>", "(IIILjava/util/List;Ljava/util/Map;)V");

    jint major = (jint) VK_API_VERSION_MAJOR(deviceApiVersion);
    jint minor = (jint) VK_API_VERSION_MINOR(deviceApiVersion);
    jint patch = (jint) VK_API_VERSION_PATCH(deviceApiVersion);

    LOG_I("Final device Vulkan version: %d.%d.%d", major, minor, patch);

    jobject result = (*env)->NewObject(env, capClass, capInit,
                                       major, minor, patch,
                                       extensionsList, featuresMap);

    (*env)->DeleteLocalRef(env, listClass);
    (*env)->DeleteLocalRef(env, mapClass);
    (*env)->DeleteLocalRef(env, boolClass);
    (*env)->DeleteLocalRef(env, capClass);

    pvkDestroyInstance(instance, NULL);
    dlclose(vulkan_handle);

    LOG_I("Check finished. Vulkan %d.%d.%d", major, minor, patch);
    return result;
}
