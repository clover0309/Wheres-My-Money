package com.byul.wheresmymoney.backend.API;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.byul.wheresmymoney.backend.Stock.Dto.StockDTO.AddStockRequest;
import com.byul.wheresmymoney.backend.Stock.Dto.StockDTO.StockSearchResponse;
import com.byul.wheresmymoney.backend.Stock.Dto.UserStockDTO;
import com.byul.wheresmymoney.backend.Stock.Service.StockSearchService;
import com.byul.wheresmymoney.backend.Stock.Service.UserStockService;
import com.byul.wheresmymoney.backend.User.Dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockApiController {

    @Value("${kiwoom.appkey}")
    private String appkey;

    @Value("${kiwoom.secretkey}")
    private String secretkey;
    
    private final StockSearchService stockSearchService;
    private final UserStockService userStockService;

    @PostMapping("/test-connection")
    public ResponseEntity<ApiResponse> testKiwoomConnection() {
        try {
            String host = "https://api.kiwoom.com";
            String endpoint = "/oauth2/token";
            String urlString = host + endpoint;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);

            String jsonData = String.format(
                "{\"grant_type\":\"client_credentials\",\"appkey\":\"%s\",\"secretkey\":\"%s\"}",
                appkey,
                secretkey
            );
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
                    String responseBody = scanner.useDelimiter("\\A").next();
                    
                    // JSON을 재 파싱하여 민감한 정보 제거.
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                    
                    // 민감한 정보 제거시 객체.remove를 사용함.
                    responseMap.remove("appkey");
                    responseMap.remove("secretkey");
                    
                    // 필요한 정보만 담은 새로운 HashMap 생성후, 다시 응답에 포함 시킴.
                    Map<String, Object> filteredData = new HashMap<>();
                    filteredData.put("access_token_token_expired", responseMap.get("access_token_token_expired"));
                    filteredData.put("token_type", responseMap.get("token_type"));
                    filteredData.put("expires_in", responseMap.get("expires_in"));
                    
                    return ResponseEntity.ok(new ApiResponse(true, "연결 완료", filteredData));
                }
            } else {
                return ResponseEntity.ok(new ApiResponse(false, "연결 실패: HTTP " + responseCode));
            }

        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "연결 실패: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<StockSearchResponse>> searchStock(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().length() < 1) {
            return ResponseEntity.badRequest().build();
        }
        
        List<StockSearchResponse> results = stockSearchService.searchStocks(keyword);
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addStock(@RequestBody AddStockRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(false, "로그인이 필요합니다."));
        }
        
        String userId = request.getUserId();
        boolean success = userStockService.addStock(userId, request);
        
        if (success) {
            return ResponseEntity.ok(new ApiResponse(true, "주식이 추가되었습니다."));
        } else {
            return ResponseEntity.ok(new ApiResponse(false, "주식 추가에 실패했습니다."));
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<UserStockDTO>> getUserStocks(@RequestParam String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<UserStockDTO> stocks = userStockService.getUserStocks(userId);
        return ResponseEntity.ok(stocks);
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteStock(@RequestParam Integer stockIdx, @RequestParam String userId) {
        if (stockIdx == null || userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(false, "잘못된 요청입니다."));
        }
        
        boolean success = userStockService.deleteStock(stockIdx, userId);
        
        if (success) {
            return ResponseEntity.ok(new ApiResponse(true, "주식이 삭제되었습니다."));
        } else {
            return ResponseEntity.ok(new ApiResponse(false, "주식 삭제에 실패했습니다."));
        }
    }
}
