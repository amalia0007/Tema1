package com.javaweb.tema1;

import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/tasks")
public class TaskController {
    @Autowired
    private TaskService service;

    /**
     * TEMA 1 - Stefanescu Amalia-Andreea
     * Continuand API-ul REST realizat la laborator, implementati un
     * endpoint care sa permita exportul taskurilor, filtrate dupa diverse criterii,
     * cu posibilitatea selectiei unei liste de campuri prezente in raspuns,
     * rezultatul fiind intors intr-un format indicat, fie el csv sau xml.
     */
    @GetMapping
    public ResponseEntity<List<Object>> exportTasksToFile(@RequestParam(required = false) String title,
                                                          @RequestParam(required = false) String description,
                                                          @RequestParam(required = false) String assignedTo,
                                                          @RequestParam(required = false) TaskModel.TaskStatus status,
                                                          @RequestParam(required = false) TaskModel.TaskSeverity severity,
                                                          @RequestHeader(required = false, name = "X-Fields") String fields,
                                                          @RequestHeader(required = false, name = "X-File") String fileFormat,
                                                          @RequestHeader(required = false, name = "X-Filter") String filter) throws IOException {
        List<TaskModel> tasks = service.getTasks(title, description, assignedTo, status, severity);
        if (tasks.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            List<Object> items;

            tasks = filterFields(filter, tasks);

            items = selectFields(fields, tasks);

            exportToFile(fileFormat, items);

            return ResponseEntity.ok(items);
        }
    }

    private void exportToFile(String fileFormat, List<Object> items) throws IOException {
        if (fileFormat != null && !fileFormat.isEmpty()) {
            if (fileFormat.equalsIgnoreCase("csv")) {
                exportToCsvFile(items);
            }

            if (fileFormat.equalsIgnoreCase("xml")) {
                exportToXmlFile(items);
            }
        }
    }

    private void exportToXmlFile(List<Object> items) {
        try {
            Tasks tasks = new Tasks();
            File file = new File("xmlFile.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Tasks.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            for (Object item : items) {
                tasks.getModels().add((TaskModel) item);
            }
            jaxbMarshaller.marshal(tasks, file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void exportToCsvFile(List<Object> items) throws IOException {
        File file = new File("csvFile.csv");
        try {
            FileWriter fileWriter = new FileWriter(file);
            CSVWriter writer = new CSVWriter(fileWriter);

            List<String[]> data = new ArrayList<>();
            for (Object item : items) {
                data.add(new String[]{item.toString()});
            }
            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<TaskModel> filterFields(String filter, List<TaskModel> tasks) {
        if (filter != null && !filter.isBlank()) {
            if (isAnyMatchTaskStatus(filter)) {
                tasks = tasks
                        .stream()
                        .filter(task -> task.getStatus().name().equalsIgnoreCase(filter))
                        .collect(Collectors.toList());
            }

            if (isAnyMatchTaskSeverity(filter)) {
                tasks = tasks
                        .stream()
                        .filter(task -> task.getSeverity().name().equalsIgnoreCase(filter))
                        .collect(Collectors.toList());
            }
        }
        return tasks;
    }

    private boolean isAnyMatchTaskSeverity(String filter) {
        return Arrays.stream(TaskModel.TaskSeverity.values()).anyMatch(val -> val.name().equalsIgnoreCase(filter));
    }

    private boolean isAnyMatchTaskStatus(String filter) {
        return Arrays.stream(TaskModel.TaskStatus.values()).anyMatch(val -> val.name().equalsIgnoreCase(filter));
    }

    private List<Object> selectFields(String fields, List<TaskModel> tasks) {
        List<Object> items;
        if (fields != null && !fields.isBlank()) {
            items = tasks.stream().map(task -> task.sparseFields(fields.split(","))).collect(Collectors.toList());
        } else {
            items = new ArrayList<>(tasks);
        }
        return items;
    }

}
