export interface BusinessRequest {
  ownerName: string;
  mobileNumber: string;
  businessName: string;
  businessType: string;
  city: string;
  state: string;
  preferredLanguage: string;
  address?: string;
  pinCode?: string;
  gstNumber?: string;
}

export interface BusinessResponse {
  id: number;
  ownerName: string;
  mobileNumber: string;
  name: string;
  businessType: string;
  city: string;
  state: string;
  preferredLanguage: string;
  address?: string;
  pinCode?: string;
  gstNumber?: string;
}

export const BUSINESS_TYPES = [
  'GROCERY', 'RETAIL', 'WHOLESALE', 'RESTAURANT', 'PHARMACY',
  'ELECTRONICS', 'TEXTILE', 'AUTOMOTIVE', 'CONSTRUCTION',
  'MANUFACTURING', 'SERVICES', 'TRADING', 'OTHER',
];

export const LANGUAGES = [
  { value: 'HINDI',     label: 'हिंदी (Hindi)' },
  { value: 'ENGLISH',   label: 'English' },
  { value: 'MARATHI',   label: 'मराठी (Marathi)' },
  { value: 'GUJARATI',  label: 'ગુજરાતી (Gujarati)' },
  { value: 'TAMIL',     label: 'தமிழ் (Tamil)' },
  { value: 'TELUGU',    label: 'తెలుగు (Telugu)' },
  { value: 'KANNADA',   label: 'ಕನ್ನಡ (Kannada)' },
  { value: 'BENGALI',   label: 'বাংলা (Bengali)' },
  { value: 'MALAYALAM', label: 'മലയാളം (Malayalam)' },
  { value: 'PUNJABI',   label: 'ਪੰਜਾਬੀ (Punjabi)' },
];

export const INDIAN_STATES = [
  'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh',
  'Goa', 'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jharkhand', 'Karnataka',
  'Kerala', 'Madhya Pradesh', 'Maharashtra', 'Manipur', 'Meghalaya', 'Mizoram',
  'Nagaland', 'Odisha', 'Punjab', 'Rajasthan', 'Sikkim', 'Tamil Nadu',
  'Telangana', 'Tripura', 'Uttar Pradesh', 'Uttarakhand', 'West Bengal',
  'Delhi', 'Jammu & Kashmir', 'Ladakh', 'Puducherry',
];
