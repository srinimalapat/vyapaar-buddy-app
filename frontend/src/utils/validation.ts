export const validateIndianMobile = (value: string): string | null => {
  const cleaned = value.replace(/\D/g, '').replace(/^91/, '');
  if (!cleaned) return 'Mobile number is required';
  if (!/^[6-9]\d{9}$/.test(cleaned)) return 'Enter a valid 10-digit Indian mobile number (start with 6-9)';
  return null;
};

export const required = (value: string, label = 'This field'): string | null => {
  return value.trim() ? null : `${label} is required`;
};

export const validateEmail = (value: string): string | null => {
  if (!value) return null;
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? null : 'Enter a valid email address';
};

export const validateMinLength = (value: string, min: number, label = 'Field'): string | null => {
  return value.length >= min ? null : `${label} must be at least ${min} characters`;
};
