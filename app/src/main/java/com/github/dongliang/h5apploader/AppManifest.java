package com.github.dongliang.h5apploader;

import java.util.List;

/**
 * Created by dongliang on 2015/12/31.
 */
public class AppManifest {

    public String startup;
    public String bundle;
    public String version;


    public List<FileItem> files;

    public class FileItem{

        public static final int NONE = 0;
        public static final int NEW = 1;
        public static final int UPDATE = 2;
        public static final int DELETE = 3;

        public String path;
        public String hash;
        public int size;
        public int modifiedType;

        @Override
        public boolean equals(Object object){
            FileItem item = (FileItem)object;
            return this.path.equals(item.path);
        }

        @Override
        public int hashCode(){
            return path.hashCode();
        }
    }
}
