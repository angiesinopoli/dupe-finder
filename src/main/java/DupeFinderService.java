package com.example.dupeFinder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DupeFinderService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> findDupes(String productName) throws Exception {
        // Step 1: find the target product and its ingredients
        String searchUrl = "https://world.openbeautyfacts.org/cgi/search.pl"
                + "?search_terms=" + productName
                + "&search_simple=1&action=process&json=1&page_size=10";

        String response = restTemplate.getForObject(searchUrl, String.class);
        JsonNode products = mapper.readTree(response).path("products");

        Set<String> targetIngredients = new HashSet<>();
        String targetName = "";
        String targetCategory = "";

        for (JsonNode p : products) {
            JsonNode ingredients = p.path("ingredients_tags");
            if (ingredients.size() > 3) {
                targetName = p.path("product_name").asText();
                targetCategory = p.path("categories_tags").path(0).asText();
                for (JsonNode ing : ingredients) {
                    targetIngredients.add(ing.asText());
                }
                break;
            }
        }

        if (targetIngredients.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 2: fetch a broader pool using the category
        String poolUrl = "https://world.openbeautyfacts.org/cgi/search.pl"
                + "?search_terms=foundation"
                + "&search_simple=1&action=process&json=1&page_size=100";

        String poolResponse = restTemplate.getForObject(poolUrl, String.class);
        JsonNode poolProducts = mapper.readTree(poolResponse).path("products");

        // Step 3: score each product
        List<Map<String, Object>> dupes = new ArrayList<>();
        final String finalTargetName = targetName;

        for (JsonNode p : poolProducts) {
            String name = p.path("product_name").asText();
            if (name.equals(finalTargetName)) continue;

            Set<String> otherIngredients = new HashSet<>();
            for (JsonNode ing : p.path("ingredients_tags")) {
                otherIngredients.add(ing.asText());
            }

            if (otherIngredients.size() < 3) continue;

            Set<String> intersection = new HashSet<>(targetIngredients);
            intersection.retainAll(otherIngredients);

            Set<String> union = new HashSet<>(targetIngredients);
            union.addAll(otherIngredients);

            double similarity = (double) intersection.size() / union.size();

            if (similarity > 0.1) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("product_name", name);
                result.put("similarity_score", Math.round(similarity * 100) + "%");
                result.put("shared_ingredients", intersection.size());
                result.put("shared_ingredient_list", new ArrayList<>(intersection));
                dupes.add(result);
            }
        }

        // Step 4: sort by shared ingredients descending
        dupes.sort((a, b) -> (int) b.get("shared_ingredients") - (int) a.get("shared_ingredients"));

        return dupes;
    }
}