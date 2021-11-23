package com.javaweb.tema1;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Tasks {
    List<TaskModel> models = new ArrayList<>();

    public List<TaskModel> getModels() {
        return models;
    }

    public void setModels(List<TaskModel> models) {
        this.models = models;
    }
}
