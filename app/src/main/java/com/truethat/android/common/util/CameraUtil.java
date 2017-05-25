package com.truethat.android.common.util;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

public class CameraUtil {
    public static String getFrontFacingCameraId(CameraManager manager) {
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer orientation = characteristics
                        .get(CameraCharacteristics.LENS_FACING);
                if (orientation == null) continue;
                if (orientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
