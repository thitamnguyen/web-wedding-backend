package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //luu theo user_id
    // Nếu map thẳng liên kết sang bảng Người Dùng (User/Account)
    // Sửa lại đoạn này trong Booking.java để không bị lỗi trùng cột user_id
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "fitting_date")
    private LocalDate fittingDate;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "total_price")
    private Double totalPrice = 0.0;

    @Column(name = "discount_percentage")
    private Double discountPercentage = 0.0;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "promotion_code")
    private String promotionCode;

    @Column(name = "promotion_name")
    private String promotionName;

    @Column(name = "payment_status")
    private String paymentStatus = "UNPAID";

    private String status = "PENDING";

    //code sepay
    @Column(name = "payment_method")
    private String paymentMethod; // "CASH" (Tiền mặt), "BANK_TRANSFER" (Chuyển khoản)

    @Column(name = "deposit_amount")
    private Double depositAmount = 0.0; // Số tiền cọc cần thu (20% giá trị gói)

    @Column(name = "remaining_amount")
    private Double remainingAmount = 0.0; // Số tiền còn lại phải thu sau này (80%)

    @Column(name = "deposit_method")
    private String depositMethod; // "AUTO_SEPAY", "MANUAL_CASH", "MANUAL_BANK"

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================================================================
    // CẤU HÌNH QUAN HỆ ĐỐI TƯỢNG (JOIN BẢNG) ĐỂ LẤY TÊN HIỂN THỊ
    // =========================================================================

    @Column(name = "service_id")
    private Long serviceId;

    @ManyToOne
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private WeddingService weddingService; // Lấy thông tin gói dịch vụ cưới

    @Column(name = "photographer_id")
    private Long photographerId;

    @ManyToOne
    @JoinColumn(name = "photographer_id", insertable = false, updatable = false)
    private Profile photographerProfile; // Lấy thông tin nhiếp ảnh gia từ bảng profiles

    @Column(name = "makeup_artist_id")
    private Long makeupArtistId;

    @ManyToOne
    @JoinColumn(name = "makeup_artist_id", insertable = false, updatable = false)
    private MakeupArtist makeupArtist; // Lấy thông tin makeup artist từ bảng beauty_experts

    @Column(name = "is_read")
    private Boolean isRead = false; // Mặc định đơn hàng mới tạo là CHƯA ĐỌC thông báo

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public WeddingService getWeddingService() {
        return weddingService;
    }

    public void setWeddingService(WeddingService weddingService) {
        this.weddingService = weddingService;
    }

    public Long getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(Long photographerId) {
        this.photographerId = photographerId;
    }

    public Profile getPhotographerProfile() {
        return photographerProfile;
    }

    public void setPhotographerProfile(Profile photographerProfile) {
        this.photographerProfile = photographerProfile;
    }

    public Long getMakeupArtistId() {
        return makeupArtistId;
    }

    public void setMakeupArtistId(Long makeupArtistId) {
        this.makeupArtistId = makeupArtistId;
    }

    public MakeupArtist getMakeupArtist() {
        return makeupArtist;
    }

    public void setMakeupArtist(MakeupArtist makeupArtist) {
        this.makeupArtist = makeupArtist;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getFittingDate() {
        return fittingDate;
    }

    public void setFittingDate(LocalDate fittingDate) {
        this.fittingDate = fittingDate;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(Double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public String getDepositMethod() {
        return depositMethod;
    }

    public void setDepositMethod(String depositMethod) {
        this.depositMethod = depositMethod;
    }
}
