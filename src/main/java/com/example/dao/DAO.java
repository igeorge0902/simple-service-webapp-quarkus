package com.example.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.api.Sessions;

/**
 * Builds a Sessions model from parallel lists of session IDs and device IDs.
 */
public class DAO {

    private final Map<String, Sessions> sessionProvider = new LinkedHashMap<>();

    public DAO(List<String> sessionIds, List<String> deviceIds) {
        int size = Math.min(sessionIds.size(), deviceIds.size());
        for (int i = 0; i < size; i++) {
            Sessions session = new Sessions(Integer.toString(i), sessionIds.get(i), deviceIds.get(i));
            sessionProvider.put(Integer.toString(i), session);
        }
    }

    public Map<String, Sessions> getModel() {
        return sessionProvider;
    }

    public List<Sessions> getSessionsList() {
        return new ArrayList<>(sessionProvider.values());
    }
}
