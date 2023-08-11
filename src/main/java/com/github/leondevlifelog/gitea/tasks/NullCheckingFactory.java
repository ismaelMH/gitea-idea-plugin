package com.github.leondevlifelog.gitea.tasks;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.io.mandatory.JsonMandatoryException;
import org.jetbrains.io.mandatory.Mandatory;
import org.jetbrains.io.mandatory.RestModel;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author Mikhail Golubev
 */
public class NullCheckingFactory implements TypeAdapterFactory {
    public static final NullCheckingFactory INSTANCE = new NullCheckingFactory();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
        if (type.getRawType().getAnnotation(RestModel.class) == null) {
            return null;
        }
        final TypeAdapter<T> defaultAdapter = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                defaultAdapter.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                T stub = defaultAdapter.read(in);
                if (stub == null) return null;

                for (Field field : ReflectionUtil.collectFields(type.getRawType())) {
                    if (field.getAnnotation(Mandatory.class) != null) {
                        try {
                            field.setAccessible(true);
                            if (field.get(stub) == null) {
                                throw new JsonMandatoryException(String.format("Field '%s' is mandatory, but missing in response", field.getName()));
                            }
                        }
                        catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return stub;
            }
        };
    }
}
