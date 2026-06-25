package com.example.demo.service;

import com.example.demo.model.WeddingService;
import com.example.demo.model.Promotion;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.ProductItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatService {

    //private final String apiKey = "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";
    private final WebClient webClient = WebClient.builder().build();

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    public Map<String, Object> getAIResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase().trim();

        // 1. [GIỮ NGUYÊN] LẤY TOÀN BỘ DANH SÁCH GÓI DỊCH VỤ THỰC TẾ
        List<WeddingService> listServices = new ArrayList<>();
        try {
            listServices = serviceRepository.findAll();
        } catch (Exception e) {
            System.err.println("Lỗi lấy gói chụp: " + e.getMessage());
        }

        // LỌC GÓI THEO NGÂN SÁCH NGƯỜI DÙNG (GIỮ NGUYÊN)
        long userBudget = extractBudget(lowerMessage);
        StringBuilder dynamicServicesContext = new StringBuilder("DANH SÁCH CÁC GÓI CHỤP THỰC TẾ TRONG DATABASE CỦA STUDIO:\n");

        if (userBudget > 0) {
            List<WeddingService> affordableServices = listServices.stream()
                    .filter(s -> {
                        long packagePrice = parsePriceToLong(s.getPriceRange());
                        return packagePrice > 0 && packagePrice <= userBudget;
                    })
                    .collect(Collectors.toList());

            if (!affordableServices.isEmpty()) {
                dynamicServicesContext.append("(LƯU Ý: Khách hàng đang có ngân sách tối đa là ").append(userBudget).append("đ. ĐÂY LÀ CÁC GÓI PHÙ HỢP HOẶC THẤP HƠN TIỀN CỦA KHÁCH, BẠN CHỈ ĐƯỢC TƯ VẤN CÁC GÓI NÀY):\n");
                for (WeddingService s : affordableServices) {
                    dynamicServicesContext.append("- GÓI ID: ").append(s.getId()).append(", Tên gói: ").append(s.getTitle()).append(", Giá cả: ").append(s.getPriceRange()).append(", Mô tả: ").append(s.getShortDescription()).append("\n");
                }
            } else {
                dynamicServicesContext.append("(LƯU Ý: Khách hàng có ngân sách ").append(userBudget).append("đ nhưng không có gói nào thấp hơn mức này. Hãy khéo léo giới thiệu gói thấp nhất hiện có của studio dưới đây):\n");
                for (WeddingService s : listServices) {
                    dynamicServicesContext.append("- GÓI ID: ").append(s.getId()).append(", Tên gói: ").append(s.getTitle()).append(", Giá cả: ").append(s.getPriceRange()).append("\n");
                }
            }
        } else {
            for (WeddingService s : listServices) {
                dynamicServicesContext.append("- GÓI ID: ").append(s.getId()).append(", Tên gói chính xác: ").append(s.getTitle()).append(", Giá cả: ").append(s.getPriceRange()).append(", Mô tả dịch vụ: ").append(s.getShortDescription()).append("\n");
            }
        }

        // 2. [GIỮ NGUYÊN] BỘ LỌC Ý ĐỊNH CHỐT ĐƠN & ĐƯỜNG DẪN BOOKING
        boolean isClosingIntent = lowerMessage.contains("chốt") || lowerMessage.contains("đặt lịch") || lowerMessage.contains("đăng ký") || lowerMessage.contains("book") || lowerMessage.contains("lấy gói");
        if (isClosingIntent) {
            Long matchedServiceId = null;
            String matchedTitle = "";
            for (WeddingService s : listServices) {
                String titleLower = s.getTitle().toLowerCase();
                if (lowerMessage.contains(titleLower) || lowerMessage.contains(titleLower.replace("gói chụp ảnh cưới ", "")) || lowerMessage.contains(titleLower.replace("gói ", ""))) {
                    matchedServiceId = s.getId();
                    matchedTitle = s.getTitle();
                    break;
                }
            }
            if (matchedServiceId == null && !listServices.isEmpty()) {
                matchedServiceId = listServices.get(0).getId();
                matchedTitle = listServices.get(0).getTitle();
            } else if (matchedServiceId == null) {
                matchedServiceId = 1L;
                matchedTitle = "Gói Chụp Ảnh Cưới";
            }
            String closingReply = "✨ Tuyệt vời quá! LuxeAI Studio rất vinh hạnh được đồng hành cùng hai bạn trong ngày trọng đại.<br/><br/>"
                    + "💎 Hai bạn đã lựa chọn: <b>" + matchedTitle + "</b><br/>"
                    + "👉 Xin mời hai bạn điền thông tin đặt lịch vào đây: "
                    + "<a href=\"http://localhost:5173/booking\" style=\"color: #ff4766; font-weight: bold; text-decoration: underline;\">Form Đăng Ký Đặt Lịch Chụp Ảnh Cưới</a><br/><br/>"
                    + "Ekip Studio sẽ liên hệ lại ngay với hai bạn sau khi nhận được thông tin nhé! 🥰";
            return Map.of("reply", closingReply);
        }

        // 3. [GIỮ NGUYÊN] LẤY DANH SÁCH ƯU ĐÃI ĐANG KÍCH HOẠT TỪ DATABASE
//        StringBuilder dynamicPromotionsContext = new StringBuilder("DANH SÁCH CHƯƠNG TRÌNH ƯU ĐÃI / KHUYẾN MÃI ĐANG CÓ HIỆU LỰC:\n");
//        try {
//            List<Promotion> activePromotions = promotionRepository.findActivePromotionsByDate(LocalDate.now());
//            if (activePromotions.isEmpty()) {
//                activePromotions = promotionRepository.findAll().stream().filter(Promotion::getActive).collect(Collectors.toList());
//            }
//            if (activePromotions.isEmpty()) {
//                dynamicPromotionsContext.append("- Hiện tại hệ thống chưa kích hoạt chương trình ưu đãi nào mới.\n");
//            } else {
//                for (Promotion p : activePromotions) {
//                    dynamicPromotionsContext.append("- Mã/Chương trình: ").append(p.getName())
//                            .append(" | Code: ").append(p.getId() != null ? p.getId() : "Áp dụng tự động")
//                            .append(" | Chi tiết: ").append(p.getDescription())
//                            .append(" | Giảm giá: ").append(p.getDiscountPercentage()).append("%\n");
//                }
//            }
//        } catch (Exception e) {
//            dynamicPromotionsContext.append("- Không thể kết nối dữ liệu khuyến mãi lúc này.\n");
//        }

        // 4. [GIỮ NGUYÊN] XỬ LÝ LOGIC CONCEPT ĐỊNH VỊ CONCEPT MỚI NHẤT VÀ RẺ NHẤT
        StringBuilder dynamicConceptsContext = new StringBuilder("DỮ LIỆU ĐỘNG VỀ CÁC CONCEPT NỔI BẬT ĐANG CÓ TẠI STUDIO:\n");
        try {
            List<ProductItem> allProducts = productItemRepository.findAll();
            List<ProductItem> conceptItems = allProducts.stream()
                    .filter(item -> item.getCategoryKey() != null && item.getCategoryKey().toLowerCase().contains("concept"))
                    .collect(Collectors.toList());

            dynamicConceptsContext.append("- Tổng số lượng Concept hiện có: ").append(conceptItems.size()).append(" concept.\n");

            if (!conceptItems.isEmpty()) {
                ProductItem newestConcept = conceptItems.stream().max(Comparator.comparing(ProductItem::getId)).orElse(conceptItems.get(0));
                ProductItem cheapestConcept = conceptItems.stream().min(Comparator.comparingLong(c -> parsePriceToLong(c.getPriceRange()))).orElse(conceptItems.get(0));

                dynamicConceptsContext.append("- [MỚI NHẤT / THỊNH HÀNH NHẤT]: ").append(newestConcept.getTitle()).append(" (Giá: ").append(newestConcept.getPriceRange()).append("). Mô tả: ").append(newestConcept.getExcerpt()).append("\n");
                dynamicConceptsContext.append("- [RẺ NHẤT / TIẾT KIỆM NHẤT]: ").append(cheapestConcept.getTitle()).append(" (Giá: ").append(cheapestConcept.getPriceRange()).append("). Mô tả: ").append(cheapestConcept.getExcerpt()).append("\n");

                dynamicConceptsContext.append("- Danh sách tất cả các mẫu Concept khác để tham khảo:\n");
                for (ProductItem c : conceptItems) {
                    dynamicConceptsContext.append("  + ").append(c.getTitle()).append(" | Giá: ").append(c.getPriceRange()).append("\n");
                }
            } else {
                dynamicConceptsContext.append("- Hiện chưa cập nhật mẫu concept nào cụ thể lên website.\n");
            }
        } catch (Exception e) {
            dynamicConceptsContext.append("- Lỗi nạp dữ liệu phân tích mẫu concept.\n");
        }

        // 5. [THÊM MỚI SIÊU NÂNG CẤP] BỘ TRI THỨC NGHIỆP VỤ STUDIO ĐỘC QUYỀN TRẢ LỜI QUY TRÌNH & ĐIỀU KHOẢN TRUY VẤN
        String studioPolicyKnowledge = "BỘ QUY ĐỊNH VÀ QUY TRÌNH NGHIỆP VỤ CỦA LUXEAI STUDIO (DÙNG ĐỂ TRẢ LỜI KHI KHÁCH HỎI):\n"
                + "+ QUY TRÌNH TRẢ ẢNH: Toàn bộ file ảnh gốc chất lượng cao sẽ được gửi cho hai bạn sau 2 ngày chụp qua Google Drive. Ảnh đám cưới được chỉnh sửa photoshop hoàn thiện và bàn giao album cao cấp sau 14 ngày. Hỗ trợ sửa gấp trong 3 ngày làm lễ cưới có phụ phí 500.000đ.\n"
                + "+ SỰ CỐ THỜI TIẾT / HỦY LỊCH: Nếu ngày chụp gặp trời mưa bão lớn không thể thực hiện ngoại cảnh, Studio hỗ trợ hai bạn dời lịch sang ngày khác miễn phí hoàn toàn, hoặc chuyển vào chụp concept tại studio/phim trường. Nếu hai bạn bận việc đột xuất muốn dời lịch, vui lòng báo trước cho ekip ít nhất 5 ngày.\n"
                + "+ CHI PHÍ DI CHUYỂN, VÉ VÀO CỔNG: Studio luôn chuẩn bị xe 16 chỗ đưa đón toàn bộ cô dâu chú rể và ekip trong suốt ngày chụp. Các chi phí về vé vào cổng phim trường hay vé các địa điểm tham quan đã bao gồm trọn gói trong dịch vụ (khách hàng không cần trả thêm).\n"
                + "+ ĐỒ TRANG PHỤC VÀ TRANG ĐIỂM: Cô dâu được tự do thử và chọn các mẫu váy cưới trong BST mới nhất. Chú rể được mượn vest cao cấp vừa size. Đi chụp ngoại cảnh xa, thợ chuyên gia makeup của studio luôn đi theo hỗ trợ dặm phấn, thay đổi layout trang điểm và làm 2 - 3 kiểu tóc phù hợp với từng trang phục.\n"
                + "+ PHƯƠNG THỨC THANH TOÁN & ĐẶT CỌC: Để giữ lịch chụp, hai bạn sẽ đặt cọc trước 20% giá trị gói dịch vụ. Số tiền còn lại 70% hai bạn sẽ thanh toán vào ngày đi chụp ảnh trực tiếp tại studio hoặc chuyển khoản.";

        // 6. THIẾT LẬP SYSTEM CONTEXT TỔNG HỢP TOÀN BỘ NĂNG LỰC CỦA BOT
        String systemContext = "Bạn là trợ lý ảo tư vấn thông minh 'LuxeAI' của Studio chụp ảnh cưới.\n"
                + "XƯNG HÔ: Luôn ngọt ngào, xưng 'LuxeAI' hoặc 'Studio' và gọi khách hàng là 'Hai bạn'.\n"
                + "PHONG CÁCH: Trả lời ngắn gọn, chia nhỏ thành các dòng bằng dấu xuống dòng (\\n), sử dụng emoji sinh động (📸, 💎, ✨, 👉).\n\n"
                + "DỮ LIỆU THỰC TẾ TỪ DATABASE:\n"
                + dynamicServicesContext.toString() + "\n"
//                + dynamicPromotionsContext.toString() + "\n"
                + dynamicConceptsContext.toString() + "\n"
                + "BỘ TRI THỨC ĐIỀU KHOẢN STUDIO:\n"
                + studioPolicyKnowledge + "\n\n"
                + "QUY TẮC BẮT BUỘC KHI TƯ VẤN:\n"
                + "1. Khi khách hỏi về khuyến mãi/mã giảm giá: Liệt kê đầy đủ các mã đang hiển thị ở danh sách ưu đãi phía trên.\n"
                + "2. Khi khách hỏi về Concept MỚI NHẤT hoặc THỊNH HÀNH nhất: Bạn bắt buộc phải chỉ đích danh tên bản ghi được đánh dấu là [MỚI NHẤT / THỊNH HÀNH NHẤT].\n"
                + "3. Khi khách hỏi về Concept RẺ NHẤT hoặc GIÁ THẤP NHẤT: Bạn bắt buộc phải chỉ đích danh tên bản ghi được đánh dấu là [RẺ NHẤT / TIẾT KIỆM NHẤT].\n"
                + "4. Nếu có giới hạn ngân sách đi kèm trong câu hỏi, hãy chỉ tư vấn những gói có mức giá bằng hoặc nhỏ hơn số tiền khách yêu cầu.\n"
                + "5. Khi khách hỏi về thời gian lấy ảnh, thời tiết mưa bão, vé phim trường, trang phục hay đặt cọc tiền: Đọc kỹ 'BỘ TRI THỨC ĐIỀU KHOẢN' để trích xuất câu trả lời chuẩn xác, tuyệt đối không tự bịa thông tin khác quy định.\n"
                + "6. Cuối câu luôn mồi chốt đơn bằng câu hỏi tinh tế: 'Hai bạn có muốn chốt đặt lịch luôn không ạ?' để kích hoạt bộ lọc chốt lịch.";

        // 7. ĐÓNG GÓI GỬI LÊN OPENROUTER API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "openrouter/free");
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemContext));
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        try {
            Map<?, ?> openRouterResponse = webClient.post()
                   // .uri("https://openrouter.ai/api/v1/chat/completions")
                   // .header("Authorization", "Bearer " + apiKey)
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
                    ObjectMapper mapper = new ObjectMapper();
                    Map<?, ?> firstChoice = mapper.convertValue(choices.get(0), Map.class);
                    Map<?, ?> messageObj = mapper.convertValue(firstChoice.get("message"), Map.class);
                    return Map.of("reply", String.valueOf(messageObj.get("content")));
                }
            }
            return Map.of("reply", "LuxeAI chưa nghe rõ, hai bạn nhắn lại với mình nhé.");
        } catch (Exception e) {
            return Map.of("error", true, "message", e.getMessage());
        }
    }

    private long extractBudget(String text) {
        text = text.replaceAll("[.,đvnd]", "");
        Pattern pattern = Pattern.compile("(\\d+)\\s*(triệu|tr|trieu)?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                long number = Long.parseLong(matcher.group(1));
                String unit = matcher.group(2);
                if (unit != null && (unit.contains("triệu") || unit.contains("tr") || unit.contains("trieu"))) {
                    return number * 1_000_000;
                }
                if (number < 1000) return number * 1_000_000;
                return number;
            } catch (Exception e) { return 0; }
        }
        return 0;
    }

    private long parsePriceToLong(String priceRange) {
        if (priceRange == null) return 0;
        try {
            String clean = priceRange.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? 0 : Long.parseLong(clean);
        } catch (Exception e) {
            return 0;
        }
    }
}