package com.example.demo.service;

import com.example.demo.model.WeddingService;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Service
public class ChatService {

    private final String apiKey = "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";
    private final WebClient webClient = WebClient.builder().build();

    @Autowired
    private ServiceRepository serviceRepository;

    public Map<String, Object> getAIResponse(String userMessage) {
        // Chuẩn hóa tin nhắn của khách để quét từ khóa không phân biệt hoa thường
        String lowerMessage = userMessage.toLowerCase().trim();

        // 1. LẤY DANH SÁCH GÓI THỰC TẾ TỪ DATABASE ĐỂ ĐỐI CHIẾU
        List<WeddingService> listServices = new ArrayList<>();
        StringBuilder dynamicServicesContext = new StringBuilder("DANH SÁCH CÁC GÓI CHỤP THỰC TẾ TRONG DATABASE CỦA STUDIO:\n");
        try {
            listServices = serviceRepository.findAll();
            for (WeddingService s : listServices) {
                dynamicServicesContext.append("- GÓI ID: ").append(s.getId())
                        .append(", Tên gói chính xác: ").append(s.getTitle())
                        .append(", Giá cả: ").append(s.getPriceRange())
                        .append(", Mô tả dịch vụ: ").append(s.getShortDescription()).append("\n");
            }
        } catch (Exception e) {
            dynamicServicesContext.append("- GÓI ID: 1, Tên gói chính xác: Gói Chụp Ảnh Cưới Luxury, Giá: 25.000.000 VNĐ, Mô tả: Ngoại cảnh Đà Lạt lãng mạn\n");
        }

        // 2. BỘ LỌC Ý ĐỊNH CHỐT ĐƠN (INTENT DETECTION) - NGĂN CHẶN AI TRẢ LỜI LAN MAN
        boolean isClosingIntent = lowerMessage.contains("chốt")
                || lowerMessage.contains("đặt lịch")
                || lowerMessage.contains("đăng ký")
                || lowerMessage.contains("book")
                || lowerMessage.contains("lấy gói");

        if (isClosingIntent) {
            Long matchedServiceId = null;
            String matchedTitle = "";

            // Duyệt qua dữ liệu thật để tìm xem khách muốn chốt gói nào
            for (WeddingService s : listServices) {
                String titleLower = s.getTitle().toLowerCase();
                if (lowerMessage.contains(titleLower)
                        || lowerMessage.contains(titleLower.replace("gói chụp ảnh cưới ", ""))
                        || lowerMessage.contains(titleLower.replace("gói ", ""))) {
                    matchedServiceId = s.getId();
                    matchedTitle = s.getTitle();
                    break;
                }
            }

            // Nếu không quét được tên cụ thể, mặc định lấy gói đầu tiên trong Database
            if (matchedServiceId == null && !listServices.isEmpty()) {
                matchedServiceId = listServices.get(0).getId();
                matchedTitle = listServices.get(0).getTitle();
            } else if (matchedServiceId == null) {
                matchedServiceId = 1L;
                matchedTitle = "Gói Chụp Ảnh Cưới";
            }

            // CẬP NHẬT CHÍNH XÁC: Sử dụng link gốc http://localhost:5173/booking của bạn
            String closingReply = "✨ Tuyệt vời quá! LuxeAI Studio rất vinh hạnh được đồng hành cùng hai bạn trong ngày trọng đại.<br/><br/>"
                    + "💎 Hai bạn đã lựa chọn: <b>" + matchedTitle + "</b><br/>"
                    + "👉 Xin mời hai bạn điền thông tin đặt lịch vào đây: "
                    + "<a href=\"http://localhost:5173/booking"  + "\" style=\"color: #ff4766; font-weight: bold; text-decoration: underline;\">Form Đăng Ký Đặt Lịch Chụp Ảnh Cưới</a><br/><br/>"
                    + "Ekip Studio sẽ liên hệ lại ngay với hai bạn sau khi nhận được thông tin nhé! 🥰";

            return Map.of("reply", closingReply);
        }

        // 3. THIẾT LẬP SYSTEM PROMPT CHO KỊCH BẢN TƯ VẤN (Nếu chỉ hỏi đáp, chưa chốt)
        String systemContext = "Bạn là trợ lý ảo tư vấn thông minh 'LuxeAI' của Studio chụp ảnh cưới.\n"
                + "XƯNG HÔ: Luôn lịch sự, ngọt ngào, xưng 'LuxeAI' hoặc 'Studio' và gọi khách hàng là 'Hai bạn'.\n"
                + "PHONG CÁCH ĐỊNH DẠNG:\n"
                + "- Trả lời ngắn gọn, tinh tế, chia nhỏ câu thành các dòng bằng cách xuống dòng (\\n) để khách hàng dễ đọc, không viết thành một cục văn bản dài dòng.\n"
                + "- Sử dụng các emoji phù hợp (📸, 💎, ✨, 👉) để văn bản sinh động.\n\n"
                + dynamicServicesContext.toString() + "\n"
                + "NHIỆM VỤ:\n"
                + "- Dựa vào ngân sách hoặc mong muốn của hai bạn để gợi ý gói chụp chính xác từ danh sách trên.\n"
                + "- Ở cuối câu trả lời tư vấn, hãy luôn chủ động mồi khách hàng chốt đơn bằng câu hỏi: 'Hai bạn có muốn chốt đặt lịch gói này luôn không ạ?'";

        // 4. ĐÓNG GÓI JSON GỬI LÊN OPENROUTER API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "openrouter/free");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemContext));
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        try {
            Map<?, ?> openRouterResponse = webClient.post()
                    .uri("https://openrouter.ai/api/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "LuxeAI Wedding Studio")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (openRouterResponse != null && openRouterResponse.containsKey("choices")) {
                List<?> choices = (List<?>) openRouterResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<?, ?> firstChoice = mapper.convertValue(choices.get(0), Map.class);
                    Map<?, ?> messageObj = mapper.convertValue(firstChoice.get("message"), Map.class);

                    String aiReply = String.valueOf(messageObj.get("content"));
                    return Map.of("reply", aiReply);
                }
            }
            return Map.of("reply", "LuxeAI chưa nghe rõ, hai bạn nhắn lại với mình nhé.");

        } catch (Exception e) {
            System.err.println("Lỗi kết nối OpenRouter: " + e.getMessage());
            return Map.of("error", true, "message", e.getMessage());
        }
    }
}