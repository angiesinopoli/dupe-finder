package com.example.dupeFinder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductController {

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) throws Exception {
        String url = "https://world.openbeautyfacts.org/cgi/search.pl"
                + "?search_terms=" + name
                + "&search_simple=1"
                + "&action=process"
                + "&json=1";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JsonNode root = mapper.readTree(response);
        JsonNode products = root.path("products");

        List<Product> result = new ArrayList<>();
        for (JsonNode node : products) {
            Product p = new Product();
            p.setProduct_name(node.path("product_name").asText());
            p.setCategories(node.path("categories").asText());

            List<String> ingredients = new ArrayList<>();
            for (JsonNode ing : node.path("ingredients_tags")) {
                ingredients.add(ing.asText());
            }
            p.setIngredients_tags(ingredients);
            result.add(p);
        }

        return result;
    }
    
    @GetMapping("/dupes")
    public List<Map<String, Object>> findDupes(@RequestParam String name) throws Exception {
        DupeFinderService service = new DupeFinderService();
        return service.findDupes(name);
    }
}