-- Insert a default admin user if not present. Adjust password as needed.
-- The application currently uses plain-text password comparisons in some flows,
-- so we insert the same password used in code: Admin@123
INSERT INTO student_records (id, name, email, password, role, is_registered, enabled, mobile)
VALUES (1, 'Admin', 'admin@library.com', 'Admin@123', 'ADMIN', 'Y', true, 9999999999)
ON CONFLICT (email) DO UPDATE
  SET name = EXCLUDED.name,
      password = EXCLUDED.password,
      role = EXCLUDED.role,
      is_registered = EXCLUDED.is_registered,
      enabled = EXCLUDED.enabled,
      mobile = EXCLUDED.mobile;

