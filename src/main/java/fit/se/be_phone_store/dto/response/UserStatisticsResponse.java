package fit.se.be_phone_store.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class UserStatisticsResponse {

    private Long user_id;
    private AccountSummary account_summary;
    private OrderStatistics order_statistics;
    private RecentActivity recent_activity;
    private FavoriteCategory favorite_category;

    // Constructors
    public UserStatisticsResponse() {}

    public UserStatisticsResponse(Long user_id, AccountSummary account_summary, 
                                 OrderStatistics order_statistics, RecentActivity recent_activity,
                                 FavoriteCategory favorite_category) {
        this.user_id = user_id;
        this.account_summary = account_summary;
        this.order_statistics = order_statistics;
        this.recent_activity = recent_activity;
        this.favorite_category = favorite_category;
    }

    // Builder Pattern
    public static UserStatisticsResponseBuilder builder() {
        return new UserStatisticsResponseBuilder();
    }

    public static class UserStatisticsResponseBuilder {
        private Long user_id;
        private AccountSummary account_summary;
        private OrderStatistics order_statistics;
        private RecentActivity recent_activity;
        private FavoriteCategory favorite_category;

        public UserStatisticsResponseBuilder user_id(Long user_id) {
            this.user_id = user_id;
            return this;
        }

        public UserStatisticsResponseBuilder account_summary(AccountSummary account_summary) {
            this.account_summary = account_summary;
            return this;
        }

        public UserStatisticsResponseBuilder order_statistics(OrderStatistics order_statistics) {
            this.order_statistics = order_statistics;
            return this;
        }

        public UserStatisticsResponseBuilder recent_activity(RecentActivity recent_activity) {
            this.recent_activity = recent_activity;
            return this;
        }

        public UserStatisticsResponseBuilder favorite_category(FavoriteCategory favorite_category) {
            this.favorite_category = favorite_category;
            return this;
        }

        public UserStatisticsResponse build() {
            return new UserStatisticsResponse(user_id, account_summary, order_statistics, 
                                            recent_activity, favorite_category);
        }
    }

    // Getters and Setters
    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }
    public AccountSummary getAccount_summary() { return account_summary; }
    public void setAccount_summary(AccountSummary account_summary) { this.account_summary = account_summary; }
    public OrderStatistics getOrder_statistics() { return order_statistics; }
    public void setOrder_statistics(OrderStatistics order_statistics) { this.order_statistics = order_statistics; }
    public RecentActivity getRecent_activity() { return recent_activity; }
    public void setRecent_activity(RecentActivity recent_activity) { this.recent_activity = recent_activity; }
    public FavoriteCategory getFavorite_category() { return favorite_category; }
    public void setFavorite_category(FavoriteCategory favorite_category) { this.favorite_category = favorite_category; }

    // Inner classes
    public static class AccountSummary {
        private LocalDateTime member_since;
        private Integer total_orders;
        private Integer completed_orders;
        private Integer cancelled_orders;
        private BigDecimal total_spent;
        private Integer total_reviews;

        // Constructors
        public AccountSummary() {}
        public AccountSummary(LocalDateTime member_since, Integer total_orders, Integer completed_orders,
                            Integer cancelled_orders, BigDecimal total_spent, Integer total_reviews) {
            this.member_since = member_since;
            this.total_orders = total_orders;
            this.completed_orders = completed_orders;
            this.cancelled_orders = cancelled_orders;
            this.total_spent = total_spent;
            this.total_reviews = total_reviews;
        }

        // Getters and Setters
        public LocalDateTime getMember_since() { return member_since; }
        public void setMember_since(LocalDateTime member_since) { this.member_since = member_since; }
        public Integer getTotal_orders() { return total_orders; }
        public void setTotal_orders(Integer total_orders) { this.total_orders = total_orders; }
        public Integer getCompleted_orders() { return completed_orders; }
        public void setCompleted_orders(Integer completed_orders) { this.completed_orders = completed_orders; }
        public Integer getCancelled_orders() { return cancelled_orders; }
        public void setCancelled_orders(Integer cancelled_orders) { this.cancelled_orders = cancelled_orders; }
        public BigDecimal getTotal_spent() { return total_spent; }
        public void setTotal_spent(BigDecimal total_spent) { this.total_spent = total_spent; }
        public Integer getTotal_reviews() { return total_reviews; }
        public void setTotal_reviews(Integer total_reviews) { this.total_reviews = total_reviews; }
    }

    public static class OrderStatistics {
        private Integer pending_orders;
        private Integer processing_orders;
        private Integer shipped_orders;
        private Integer delivered_orders;

        // Constructors
        public OrderStatistics() {}
        public OrderStatistics(Integer pending_orders, Integer processing_orders, 
                             Integer shipped_orders, Integer delivered_orders) {
            this.pending_orders = pending_orders;
            this.processing_orders = processing_orders;
            this.shipped_orders = shipped_orders;
            this.delivered_orders = delivered_orders;
        }

        // Getters and Setters
        public Integer getPending_orders() { return pending_orders; }
        public void setPending_orders(Integer pending_orders) { this.pending_orders = pending_orders; }
        public Integer getProcessing_orders() { return processing_orders; }
        public void setProcessing_orders(Integer processing_orders) { this.processing_orders = processing_orders; }
        public Integer getShipped_orders() { return shipped_orders; }
        public void setShipped_orders(Integer shipped_orders) { this.shipped_orders = shipped_orders; }
        public Integer getDelivered_orders() { return delivered_orders; }
        public void setDelivered_orders(Integer delivered_orders) { this.delivered_orders = delivered_orders; }
    }

    public static class RecentActivity {
        private LastOrder last_order;
        private LastReview last_review;

        // Constructors
        public RecentActivity() {}
        public RecentActivity(LastOrder last_order, LastReview last_review) {
            this.last_order = last_order;
            this.last_review = last_review;
        }

        // Getters and Setters
        public LastOrder getLast_order() { return last_order; }
        public void setLast_order(LastOrder last_order) { this.last_order = last_order; }
        public LastReview getLast_review() { return last_review; }
        public void setLast_review(LastReview last_review) { this.last_review = last_review; }
    }

    public static class LastOrder {
        private String order_number;
        private BigDecimal total_amount;
        private String status;
        private LocalDateTime created_at;

        // Constructors
        public LastOrder() {}
        public LastOrder(String order_number, BigDecimal total_amount, String status, LocalDateTime created_at) {
            this.order_number = order_number;
            this.total_amount = total_amount;
            this.status = status;
            this.created_at = created_at;
        }

        // Getters and Setters
        public String getOrder_number() { return order_number; }
        public void setOrder_number(String order_number) { this.order_number = order_number; }
        public BigDecimal getTotal_amount() { return total_amount; }
        public void setTotal_amount(BigDecimal total_amount) { this.total_amount = total_amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getCreated_at() { return created_at; }
        public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
    }

    public static class LastReview {
        private String product_name;
        private Integer rating;
        private LocalDateTime created_at;

        // Constructors
        public LastReview() {}
        public LastReview(String product_name, Integer rating, LocalDateTime created_at) {
            this.product_name = product_name;
            this.rating = rating;
            this.created_at = created_at;
        }

        // Getters and Setters
        public String getProduct_name() { return product_name; }
        public void setProduct_name(String product_name) { this.product_name = product_name; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public LocalDateTime getCreated_at() { return created_at; }
        public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
    }

    public static class FavoriteCategory {
        private String category_name;
        private Integer order_count;

        // Constructors
        public FavoriteCategory() {}
        public FavoriteCategory(String category_name, Integer order_count) {
            this.category_name = category_name;
            this.order_count = order_count;
        }

        // Getters and Setters
        public String getCategory_name() { return category_name; }
        public void setCategory_name(String category_name) { this.category_name = category_name; }
        public Integer getOrder_count() { return order_count; }
        public void setOrder_count(Integer order_count) { this.order_count = order_count; }
    }
}

