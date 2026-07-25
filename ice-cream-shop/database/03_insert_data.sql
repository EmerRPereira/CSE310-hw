-- =====================================================
-- INSERT INITIAL DATA FOR ICE CREAM SHOP
-- =====================================================

-- =====================================================
-- 1. PAYMENT TYPES
-- =====================================================
INSERT INTO payment_types (type_name, description) VALUES
('Cash', 'Payment in cash at the store'),
('Credit Card', 'Payment using credit card'),
('Debit Card', 'Payment using debit card'),
('Pix', 'Instant payment via Pix'),
('Mobile Wallet', 'Payment via digital wallet (Google Pay, Apple Pay)')
ON CONFLICT (type_name) DO NOTHING;

-- =====================================================
-- 2. CATEGORIES
-- =====================================================
INSERT INTO categories (category_name, description) VALUES
('Classic', 'Traditional and timeless ice cream flavors'),
('Fruit', 'Fruit-based ice cream flavors'),
('Gourmet', 'Premium and specialty ice cream flavors'),
('Vegan', 'Dairy-free and plant-based options'),
('Sugar-Free', 'No added sugar, suitable for diabetics')
ON CONFLICT (category_name) DO NOTHING;

-- =====================================================
-- 3. PRODUCTS
-- =====================================================
INSERT INTO products (name, description, price, category_id, is_available) VALUES
('Chocolate', 'Rich and creamy classic chocolate ice cream', 8.99, 1, TRUE),
('Vanilla', 'Smooth and pure vanilla bean ice cream', 7.99, 1, TRUE),
('Strawberry', 'Fresh strawberry ice cream with real fruit pieces', 9.49, 2, TRUE),
('Mint Chocolate Chip', 'Refreshing mint ice cream with chocolate chips', 9.99, 1, TRUE),
('Cookie Dough', 'Vanilla ice cream with chunks of cookie dough', 10.49, 3, TRUE),
('Pistachio', 'Creamy pistachio ice cream with roasted nuts', 11.99, 3, TRUE),
('Mango', 'Tropical mango sorbet', 8.49, 2, TRUE),
('Coconut', 'Creamy coconut ice cream', 9.49, 2, TRUE),
('Vegan Chocolate', 'Plant-based chocolate ice cream', 10.99, 4, TRUE),
('Sugar-Free Vanilla', 'Vanilla ice cream with no added sugar', 9.99, 5, TRUE),
('Salted Caramel', 'Smooth caramel ice cream with sea salt', 10.99, 3, TRUE),
('Coffee', 'Rich coffee-flavored ice cream', 9.49, 1, TRUE)
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- 4. CUSTOMERS
-- =====================================================
INSERT INTO customers (name, phone, email, address, is_active) VALUES
('João Silva', '(11) 99999-1111', 'joao.silva@email.com', 'Rua das Flores, 123, São Paulo, SP', TRUE),
('Maria Oliveira', '(11) 98888-2222', 'maria.oliveira@email.com', 'Av. Principal, 456, São Paulo, SP', TRUE),
('Carlos Santos', '(11) 97777-3333', 'carlos.santos@email.com', 'Rua dos Pinheiros, 789, São Paulo, SP', TRUE),
('Ana Souza', '(11) 96666-4444', 'ana.souza@email.com', 'Alameda dos Ingleses, 101, São Paulo, SP', TRUE)
ON CONFLICT (email) DO NOTHING;

-- =====================================================
-- 5. ORDERS
-- =====================================================
INSERT INTO orders (customer_id, status, payment_type_id, delivery_address) VALUES
(1, 'Delivered', 2, 'Rua das Flores, 123, São Paulo, SP'),
(2, 'Preparing', 1, 'Av. Principal, 456, São Paulo, SP'),
(3, 'Pending', 4, 'Rua dos Pinheiros, 789, São Paulo, SP'),
(1, 'Ready', 2, 'Rua das Flores, 123, São Paulo, SP');

-- =====================================================
-- 6. ORDER ITEMS
-- =====================================================
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 2, 8.99, 17.98),
(1, 3, 1, 9.49, 9.49),
(2, 5, 1, 10.49, 10.49),
(2, 7, 2, 8.49, 16.98),
(3, 2, 3, 7.99, 23.97),
(4, 4, 1, 9.99, 9.99),
(4, 6, 1, 11.99, 11.99);

-- =====================================================
-- UPDATE ORDER TOTALS
-- =====================================================
UPDATE orders SET total_amount = (
    SELECT SUM(subtotal)
    FROM order_items
    WHERE order_id = orders.order_id
);

-- =====================================================
-- END OF FILE
-- =====================================================
