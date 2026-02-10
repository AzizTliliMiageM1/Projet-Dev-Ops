-- Flyway V2: insert sample data for local demo

INSERT INTO users (email, full_name) VALUES
('f.mayssara@gmail.com','F. Maissara'),
('mdaziz.tlili@gmail.com','M. Daziz');

INSERT INTO abonnements (user_id, title, category, price, start_date, end_date, priority, active) VALUES
(1,'Spotify Premium','music',9.99,'2024-01-01','2025-01-01',1,TRUE),
(1,'Netflix Standard','video',12.99,'2023-11-15','2024-11-15',2,TRUE),
(2,'Canal+','video',19.99,'2024-02-01',NULL,3,TRUE);
