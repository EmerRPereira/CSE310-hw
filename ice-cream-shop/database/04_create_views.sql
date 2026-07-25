-- =====================================================
-- CREATE VIEWS FOR ICE CREAM SHOP
-- =====================================================

-- =====================================================
-- 1. ORDER SUMMARY WITH CUSTOMER DETAILS
-- =====================================================
CREATE OR REPLACE VIEW vw_order_summary AS
SELECT 
    o.order_id,
    c.name AS customer_name,
    c.phone AS customer_phone,
    c.email AS customer_email,
    o.order_date,
    o.status,
    o.total_amount,
    pt.type_name AS payment_method,
    COUNT(oi.order_item_id) AS item_count,
    SUM(oi.quantity) AS total_quantity
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
LEFT JOIN payment_types pt ON o.payment_type_id = pt.payment_type_id
LEFT JOIN order_items oi ON o.order_id = oi.order_id
GROUP BY o.order_id, c.name, c.phone, c.email, o.order_date, o.status, o.total_amount, pt.type_name
ORDER BY o.order_date DESC;

-- =====================================================
-- 2. REVENUE BY PRODUCT
-- =====================================================
CREATE OR REPLACE VIEW vw_revenue_by_product AS
SELECT 
    p.product_id,
    p.name AS product_name,
    c.category_name,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.subtotal) AS total_revenue,
    COUNT(DISTINCT o.order_id) AS order_count,
    AVG(oi.unit_price) AS avg_price
FROM order_items oi
JOIN products p ON oi.product_id = p.product_id
LEFT JOIN categories c ON p.category_id = c.category_id
JOIN orders o ON oi.order_id = o.order_id
WHERE o.status != 'Cancelled'
GROUP BY p.product_id, p.name, c.category_name
ORDER BY total_revenue DESC;

-- =====================================================
-- 3. TOP CUSTOMERS
-- =====================================================
CREATE OR REPLACE VIEW vw_top_customers AS
SELECT 
    c.customer_id,
    c.name,
    c.email,
    c.phone,
    COUNT(o.order_id) AS total_orders,
    SUM(o.total_amount) AS total_spent,
    AVG(o.total_amount) AS avg_order_value,
    MAX(o.order_date) AS last_order_date
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
WHERE o.status != 'Cancelled'
GROUP BY c.customer_id, c.name, c.email, c.phone
HAVING COUNT(o.order_id) > 0
ORDER BY total_spent DESC;

-- =====================================================
-- 4. DAILY SALES REPORT
-- =====================================================
CREATE OR REPLACE VIEW vw_daily_sales AS
SELECT 
    DATE(o.order_date) AS sale_date,
    COUNT(o.order_id) AS total_orders,
    SUM(o.total_amount) AS total_revenue,
    AVG(o.total_amount) AS avg_order_value,
    COUNT(DISTINCT o.customer_id) AS unique_customers,
    COUNT(DISTINCT oi.product_id) AS unique_products_sold
FROM orders o
LEFT JOIN order_items oi ON o.order_id = oi.order_id
WHERE o.status != 'Cancelled'
GROUP BY DATE(o.order_date)
ORDER BY sale_date DESC;

-- =====================================================
-- 5. PRODUCT PERFORMANCE
-- =====================================================
CREATE OR REPLACE VIEW vw_product_performance AS
SELECT 
    p.product_id,
    p.name AS product_name,
    p.price,
    p.is_available,
    COALESCE(SUM(oi.quantity), 0) AS total_sold,
    COALESCE(SUM(oi.subtotal), 0) AS total_revenue,
    COALESCE(COUNT(DISTINCT o.order_id), 0) AS order_count,
    CASE 
        WHEN COALESCE(SUM(oi.quantity), 0) > 0 
        THEN 'Active' 
        ELSE 'Inactive' 
    END AS performance_status
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id AND o.status != 'Cancelled'
GROUP BY p.product_id, p.name, p.price, p.is_available
ORDER BY total_sold DESC;

-- =====================================================
-- END OF FILE
-- =====================================================
