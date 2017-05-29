package com.truethat.android.application.storage.internal;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 */

@SuppressWarnings("unchecked")
public class DefaultInternalStorage implements InternalStorage {
    @Override
    public void write(Context context, String fileName, Serializable data) throws IOException {
        OutputStream file   = new FileOutputStream(context.getFilesDir() + "/" + fileName);
        ObjectOutput output = new ObjectOutputStream(file);
        output.writeObject(data);
        file.close();
        output.close();
    }

    @Override
    public <T extends Serializable> T read(Context context,
                                           String fileName) throws IOException, ClassNotFoundException {
        InputStream file   = new FileInputStream(context.getFilesDir() + "/" + fileName);
        ObjectInput input  = new ObjectInputStream(file);
        T           result = (T) input.readObject();
        file.close();
        input.close();
        return result;
    }

    @Override
    public boolean exists(Context context, String fileName) {
        return new File(context.getFilesDir() + "/" + fileName).exists();
    }

    @Override
    public void delete(Context context, String fileName) throws IOException {
        File file = new File(context.getFilesDir() + "/" + fileName);
        if (!file.delete()) {
            throw new IOException(
                    "File " + fileName + " deletion from internal storage have failed.");
        }
    }
}
