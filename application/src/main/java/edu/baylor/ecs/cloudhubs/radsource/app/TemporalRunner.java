package edu.baylor.ecs.cloudhubs.radsource.app;

import com.google.gson.*;
import edu.baylor.ecs.cloudhubs.radsource.model.*;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.*;
import java.util.stream.Collectors;

public class TemporalRunner {
    public static void main (String args[]){
        File folder = new File("C:/Users/SamP9/OneDrive/Documents/GitHub/rad-source-sdg-dataset/sample_output");

        // Ensure the file exists
        if (folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                    for (File file: files){
                        if (file.isFile()){
                        try (FileReader reader = new FileReader(file)) {
                            if (file.length() == 0){
                                writeEmptyJsonFile(file);
                                continue;
                            }
                            // Parse JSON file
                            JsonElement jsonElement = JsonParser.parseReader(reader);

                            if (jsonElement != null && jsonElement.isJsonObject()) {
                                JsonObject jsonObject = jsonElement.getAsJsonObject();
                                // Extract and process the RestCalls and RestEndpoints
                                String commit = extractCommitFromPath(file.getAbsolutePath());
                                NetworkGraph sdg = processJson(jsonObject, commit);
                                Gson gson = new Gson();
                                String json = gson.toJson(sdg);
                                String filename = "C:/Users/SamP9/OneDrive/Documents/GitHub/rad-source-sdg-dataset/temp_output/network_graph_" + commit + ".json";
                                try (FileWriter fileWriter = new FileWriter(filename)) {
                                    fileWriter.write(json);
                                }
                            } else {
                                System.out.println("File " + file.getName() + " does not contain a valid JSON object.");
                            }
                            } catch (FileNotFoundException e) {
                                System.err.println("File not found: " + file.getAbsolutePath());
                                e.printStackTrace();
                            } catch (IOException e) {
                                System.err.println("I/O error reading file: " + file.getAbsolutePath());
                                e.printStackTrace();
                            } catch (JsonSyntaxException e) {
                                System.err.println("Invalid JSON syntax in file: " + file.getAbsolutePath());
                                e.printStackTrace();
                            }
                    } else {
                        System.out.println("The file does not exist or is not a file.");
                    }
                }
            }
        }
    }

    public static NetworkGraph processJson(JsonObject jsonObject, String commit) {
        List<RestCall> restCalls = new ArrayList<>();
        List<RestEndpoint> restEndpoints = new ArrayList<>();
        

        // Check if restEntityContexts exists and is an array
        if (jsonObject.has("restEntityContexts") && jsonObject.get("restEntityContexts").isJsonArray()) {
            JsonArray restEntityContexts = jsonObject.getAsJsonArray("restEntityContexts");

            for (JsonElement contextElement : restEntityContexts) {
                JsonObject contextObject = contextElement.getAsJsonObject();
                
                restCalls = extractObjects(contextObject, "restCalls", RestCall.class);
                restEndpoints = extractObjects(contextObject, "restEndpoints", RestEndpoint.class);
            }
        }
        pathArgs(restEndpoints);
        Set<String> microservices = findMicroservices(restCalls, restEndpoints);
        Set<Edge> edges = findLink(restCalls, restEndpoints, microservices);
        NetworkGraph sdg = new NetworkGraph("SDG" + commit.substring(0, 4), true, false, microservices, commit, edges);

        if (!edges.isEmpty()){
            sdg = new NetworkGraph("SDG" + commit.substring(0, 4), true, false, microservices, commit, edges);
        }
        else{
            sdg = new NetworkGraph("SDG" + commit.substring(0, 4), false, false, microservices, commit, edges);
        }

        return sdg;
    }

    public static Set<Edge> findLink(List<RestCall> restCalls, List<RestEndpoint> restEndpoints, Set<String> microservices){
        List<Edge> edges = new ArrayList<>();
        
        for (RestCall restcall : restCalls){
            for(RestEndpoint endpoint: restEndpoints){
                String restPath = parseForPath(restcall.getUrl());
                String targetNode = parseForTarget(restcall.getUrl());
                if (restPath.equals(endpoint.getPath()) && restcall.getHttpMethod().equals(endpoint.getHttpMethod()) && targetNode.equals(endpoint.getSource())){
                    edges.add(new Edge(restcall.getSource(), endpoint.getSource(), endpoint.getPath(), 0));
                }
            }
        }

        Set<Edge> edgeSet = new HashSet<>();
        Map<Edge, Long> edgeDuplicateMap = edges.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        edgeSet = edgeDuplicateMap.entrySet().stream().map(entry -> {
            Edge edge = entry.getKey();
            edge.setWeight(Math.toIntExact(entry.getValue()));
            return edge;
        }).collect(Collectors.toSet());

        return edgeSet;
    }

    public static void pathArgs (List<RestEndpoint> restEndpoints){
        for(RestEndpoint endpoint: restEndpoints){
            if (endpoint.getPath().contains("{")){
                endpoint.setPath(endpoint.getPath().replaceAll("\\{[^/]+\\}", "{var}"));
            }
        }
    }

    public static String parseForPath(String url){
        Pattern pattern = Pattern.compile("^(?:https?://[^/]+)?(/[^?#]*)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String host = matcher.group(1);
            
            // Extract the service name part before any period or colon
            Pattern hostPattern = Pattern.compile("^[^.:]+");
            Matcher hostMatcher = hostPattern.matcher(host);
            
            if (hostMatcher.find()) {
                return hostMatcher.group();
            }
        }
        return ""; // or handle this case as needed
    }
    
    public static String parseForTarget(String url){
        Pattern pattern = Pattern.compile("^(?:https?://)?([^:/]+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // or handle this case as needed
    }

    public static <T> List<T> extractObjects(JsonObject contextObject, String key, Class<T> clazz) {
        Gson gson = new Gson();
        List<T> objects = new ArrayList<>();

        if (contextObject.has(key) && contextObject.get(key).isJsonArray()) {
            JsonArray jsonArray = contextObject.getAsJsonArray(key);

            for (JsonElement element : jsonArray) {
                T obj = gson.fromJson(element, clazz);
                objects.add(obj);
            }
        }

        return objects;
    } 

    public static Set<String> findMicroservices(List<RestCall> restCalls, List<RestEndpoint> restEndpoints) {
        Set<String> microservices = new HashSet<>();

        for (RestCall restCall : restCalls) {
            String source = restCall.getSource();
            if (source != null && !source.isEmpty()) {
                String microservice = extractMicroservice(source);
                if (microservice != null) {
                    microservices.add(microservice);
                    restCall.setSource(microservice);
                }
            }
        }

        for (RestEndpoint restEndpoint : restEndpoints) {
            String source = restEndpoint.getSource();
            if (source != null && !source.isEmpty()) {
                String microservice = extractMicroservice(source);
                if (microservice != null) {
                    microservices.add(microservice);
                    restEndpoint.setSource(microservice);
                }
            }
        }

        return microservices;
    }

    public static String extractMicroservice(String source) {
        String[] parts = source.split("\\\\");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("train-ticket")) {
                if (parts[i + 1].startsWith("ts-")){
                    return parts[i + 1];
                }
                else if (!parts[i + 2].equals("src")) {
                    return parts[i + 2];
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }

    public static String extractCommitFromPath(String path) {
        int start = path.lastIndexOf("output_commit_") + "output_commit_".length();
        int end = path.lastIndexOf(".json");

        if (start != -1 && end != -1) {
            return path.substring(start, end);
        } else {
            System.out.println("Pattern not found in the string.");
            return null;
        }
    }

    private static void writeEmptyJsonFile(File file) {
        String commit = extractCommitFromPath(file.getAbsolutePath());
        String filename = "C:/Users/SamP9/OneDrive/Documents/GitHub/rad-source-sdg-dataset/temp_output/network_graph_" + commit + ".json";
        try (FileWriter fileWriter = new FileWriter(filename)) {
            fileWriter.write("{}");
        } catch (IOException e) {
            System.err.println("I/O error writing empty JSON file: " + filename);
            e.printStackTrace();
        }
    }
}

