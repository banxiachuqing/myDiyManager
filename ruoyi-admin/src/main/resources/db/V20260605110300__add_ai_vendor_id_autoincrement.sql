-- V20260605110300__add_ai_vendor_id_autoincrement.sql
-- 修正：原 V20260605110000 漏了 AUTO_INCREMENT，导致插入时 vendor_id 无默认值报错
ALTER TABLE ai_vendor
  MODIFY COLUMN vendor_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '厂商ID';
