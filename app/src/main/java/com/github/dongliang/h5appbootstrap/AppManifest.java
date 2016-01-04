package com.github.dongliang.h5appbootstrap;

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
