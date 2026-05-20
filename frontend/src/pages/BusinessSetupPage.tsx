import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Store, MapPin, Globe, ChevronRight } from 'lucide-react';
import { businessApi } from '../api/businessApi';
import { BusinessRequest, BUSINESS_TYPES, LANGUAGES, INDIAN_STATES } from '../types/business';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import { useAuth } from '../hooks/useAuth';

const STEPS = ['Business Info', 'Location', 'Preferences'];

const EMPTY_FORM: BusinessRequest = {
  ownerName: '',
  mobileNumber: '',
  businessName: '',
  businessType: 'GROCERY',
  city: '',
  state: 'Maharashtra',
  preferredLanguage: 'HINDI',
  address: '',
  pinCode: '',
  gstNumber: '',
};

const BusinessSetupPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [step, setStep] = useState(0);
  const [form, setForm] = useState<BusinessRequest>({
    ...EMPTY_FORM,
    ownerName: user?.name || '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const set = (field: keyof BusinessRequest, value: string) =>
    setForm(f => ({ ...f, [field]: value }));

  const validateStep = (): string | null => {
    if (step === 0) {
      if (!form.ownerName.trim())    return 'Owner name is required';
      if (!form.mobileNumber.trim()) return 'Mobile number is required';
      if (!/^[6-9]\d{9}$/.test(form.mobileNumber)) return 'Enter a valid 10-digit Indian mobile number';
      if (!form.businessName.trim()) return 'Business name is required';
    }
    if (step === 1) {
      if (!form.city.trim())  return 'City is required';
      if (!form.state.trim()) return 'State is required';
    }
    return null;
  };

  const handleNext = () => {
    const err = validateStep();
    if (err) { setError(err); return; }
    setError(null);
    setStep(s => s + 1);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await businessApi.createBusiness(form);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to create business profile');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-gray-100 flex items-center justify-center px-4 py-8">
      <div className="w-full max-w-lg">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 bg-primary-600 rounded-2xl mb-4">
            <Store size={28} className="text-white" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Set Up Your Business</h1>
          <p className="text-gray-500 mt-1">Tell us about your shop — takes 2 minutes</p>
        </div>

        {/* Progress steps */}
        <div className="flex items-center justify-center gap-2 mb-8">
          {STEPS.map((label, i) => (
            <React.Fragment key={label}>
              <div className="flex items-center gap-1.5">
                <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-colors ${
                  i < step  ? 'bg-primary-600 text-white' :
                  i === step ? 'bg-primary-600 text-white ring-4 ring-primary-100' :
                               'bg-gray-200 text-gray-500'
                }`}>
                  {i < step ? '✓' : i + 1}
                </div>
                <span className={`text-sm hidden sm:block ${i === step ? 'text-primary-600 font-medium' : 'text-gray-400'}`}>
                  {label}
                </span>
              </div>
              {i < STEPS.length - 1 && <div className={`flex-1 h-px max-w-12 ${i < step ? 'bg-primary-400' : 'bg-gray-200'}`} />}
            </React.Fragment>
          ))}
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-lg p-6 sm:p-8">
          {error && (
            <div className="mb-4 bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-2 text-sm">{error}</div>
          )}

          <form onSubmit={handleSubmit}>
            {/* Step 0 — Business Info */}
            {step === 0 && (
              <div className="space-y-4">
                <h2 className="font-semibold text-gray-800 flex items-center gap-2 mb-4">
                  <Store size={18} className="text-primary-600" /> Business Details
                </h2>
                <Input label="Your Name (Owner)" value={form.ownerName}
                  onChange={e => set('ownerName', e.target.value)} placeholder="e.g. Ramesh Sharma" required />
                <Input label="Mobile Number" value={form.mobileNumber}
                  onChange={e => set('mobileNumber', e.target.value)}
                  placeholder="10-digit number (6-9 start)" maxLength={10} required />
                <Input label="Business / Shop Name" value={form.businessName}
                  onChange={e => set('businessName', e.target.value)} placeholder="e.g. Ramesh Kirana Store" required />
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Business Type</label>
                  <select value={form.businessType} onChange={e => set('businessType', e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
                    {BUSINESS_TYPES.map(t => (
                      <option key={t} value={t}>{t.charAt(0) + t.slice(1).toLowerCase()}</option>
                    ))}
                  </select>
                </div>
              </div>
            )}

            {/* Step 1 — Location */}
            {step === 1 && (
              <div className="space-y-4">
                <h2 className="font-semibold text-gray-800 flex items-center gap-2 mb-4">
                  <MapPin size={18} className="text-primary-600" /> Location
                </h2>
                <Input label="City" value={form.city}
                  onChange={e => set('city', e.target.value)} placeholder="e.g. Pune" required />
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">State</label>
                  <select value={form.state} onChange={e => set('state', e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
                    {INDIAN_STATES.map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
                <Input label="Address (optional)" value={form.address || ''}
                  onChange={e => set('address', e.target.value)} placeholder="Street address" />
                <Input label="PIN Code (optional)" value={form.pinCode || ''}
                  onChange={e => set('pinCode', e.target.value)} placeholder="6-digit PIN" maxLength={6} />
              </div>
            )}

            {/* Step 2 — Preferences */}
            {step === 2 && (
              <div className="space-y-4">
                <h2 className="font-semibold text-gray-800 flex items-center gap-2 mb-4">
                  <Globe size={18} className="text-primary-600" /> Preferences
                </h2>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Preferred Language</label>
                  <select value={form.preferredLanguage} onChange={e => set('preferredLanguage', e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
                    {LANGUAGES.map(l => <option key={l.value} value={l.value}>{l.label}</option>)}
                  </select>
                  <p className="text-xs text-gray-400 mt-1">Used for WhatsApp reminder messages</p>
                </div>
                <Input label="GST Number (optional)" value={form.gstNumber || ''}
                  onChange={e => set('gstNumber', e.target.value)} placeholder="e.g. 27AAAAA0000A1Z5" />

                {/* Summary preview */}
                <div className="bg-gray-50 rounded-xl p-4 text-sm space-y-1 border border-gray-100 mt-2">
                  <p className="font-medium text-gray-700 mb-2">Review your details</p>
                  <p><span className="text-gray-500">Shop:</span> {form.businessName} ({form.businessType})</p>
                  <p><span className="text-gray-500">Owner:</span> {form.ownerName} · {form.mobileNumber}</p>
                  <p><span className="text-gray-500">Location:</span> {form.city}, {form.state}</p>
                  <p><span className="text-gray-500">Language:</span> {LANGUAGES.find(l => l.value === form.preferredLanguage)?.label}</p>
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="flex gap-3 mt-6">
              {step > 0 && (
                <Button type="button" variant="secondary" onClick={() => { setError(null); setStep(s => s - 1); }} className="flex-1">
                  Back
                </Button>
              )}
              {step < STEPS.length - 1 ? (
                <Button type="button" onClick={handleNext} className="flex-1 flex items-center justify-center gap-2">
                  Next <ChevronRight size={16} />
                </Button>
              ) : (
                <Button type="submit" isLoading={saving} className="flex-1">
                  Launch My Business 🚀
                </Button>
              )}
            </div>
          </form>
        </div>

        <p className="text-center text-xs text-gray-400 mt-4">You can update these details later from Settings</p>
      </div>
    </div>
  );
};

export default BusinessSetupPage;
