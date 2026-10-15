-- ====================================================================
-- FarmEasy Supabase PostgreSQL Schema & Migrations
-- Platform: AI-Based Smart Farmer Advisory Platform
-- ====================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. FARMER PROFILES TABLE (Linked with auth.users)
CREATE TABLE IF NOT EXISTS public.farmers (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    email TEXT,
    state TEXT DEFAULT 'Maharashtra',
    district TEXT DEFAULT 'Pune',
    preferred_language TEXT DEFAULT 'English',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS for farmers
ALTER TABLE public.farmers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Farmers can read their own profile"
    ON public.farmers FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Farmers can update their own profile"
    ON public.farmers FOR UPDATE
    USING (auth.uid() = id);

-- 3. FARMS TABLE
CREATE TABLE IF NOT EXISTS public.farms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL REFERENCES public.farmers(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    area_in_acres NUMERIC(6, 2) NOT NULL CHECK (area_in_acres > 0),
    soil_type TEXT NOT NULL,
    location_name TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_farms_farmer_id ON public.farms(farmer_id);
ALTER TABLE public.farms ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Farmers can view own farms"
    ON public.farms FOR SELECT
    USING (auth.uid() = farmer_id);

CREATE POLICY "Farmers can insert own farms"
    ON public.farms FOR INSERT
    WITH CHECK (auth.uid() = farmer_id);

CREATE POLICY "Farmers can update own farms"
    ON public.farms FOR UPDATE
    USING (auth.uid() = farmer_id);

CREATE POLICY "Farmers can delete own farms"
    ON public.farms FOR DELETE
    USING (auth.uid() = farmer_id);

-- 4. CROPS TABLE
CREATE TABLE IF NOT EXISTS public.crops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id UUID NOT NULL REFERENCES public.farms(id) ON DELETE CASCADE,
    crop_name TEXT NOT NULL,
    variety TEXT,
    sowing_date DATE NOT NULL,
    expected_harvest_date DATE,
    growth_stage TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_crops_farm_id ON public.crops(farm_id);
ALTER TABLE public.crops ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Farmers can view own crops"
    ON public.crops FOR SELECT
    USING (EXISTS (
        SELECT 1 FROM public.farms WHERE farms.id = crops.farm_id AND farms.farmer_id = auth.uid()
    ));

CREATE POLICY "Farmers can insert own crops"
    ON public.crops FOR INSERT
    WITH CHECK (EXISTS (
        SELECT 1 FROM public.farms WHERE farms.id = crops.farm_id AND farms.farmer_id = auth.uid()
    ));

CREATE POLICY "Farmers can delete own crops"
    ON public.crops FOR DELETE
    USING (EXISTS (
        SELECT 1 FROM public.farms WHERE farms.id = crops.farm_id AND farms.farmer_id = auth.uid()
    ));

-- 5. CROP DISEASE ANALYSIS TABLE
CREATE TABLE IF NOT EXISTS public.disease_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL REFERENCES public.farmers(id) ON DELETE CASCADE,
    crop_name TEXT NOT NULL,
    image_storage_path TEXT,
    condition_name TEXT NOT NULL,
    scientific_name TEXT,
    confidence NUMERIC(4, 3) NOT NULL,
    severity TEXT NOT NULL,
    symptoms TEXT,
    biological_treatment TEXT,
    chemical_treatment TEXT,
    preventive_measures TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_disease_farmer_id ON public.disease_analyses(farmer_id);
ALTER TABLE public.disease_analyses ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Farmers can view own disease analyses"
    ON public.disease_analyses FOR SELECT
    USING (auth.uid() = farmer_id);

CREATE POLICY "Farmers can insert disease analyses"
    ON public.disease_analyses FOR INSERT
    WITH CHECK (auth.uid() = farmer_id);

-- 6. SOIL ASSESSMENTS TABLE
CREATE TABLE IF NOT EXISTS public.soil_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL REFERENCES public.farmers(id) ON DELETE CASCADE,
    ph NUMERIC(3, 1) NOT NULL,
    nitrogen NUMERIC(6, 2) NOT NULL,
    phosphorus NUMERIC(6, 2) NOT NULL,
    potassium NUMERIC(6, 2) NOT NULL,
    moisture_percent NUMERIC(5, 2),
    soil_type TEXT NOT NULL,
    intended_crop TEXT NOT NULL,
    fertility_score INT NOT NULL,
    fertility_class TEXT NOT NULL,
    organic_carbon_status TEXT,
    fertilizer_recommendation TEXT NOT NULL,
    recommended_crops TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_soil_farmer_id ON public.soil_assessments(farmer_id);
ALTER TABLE public.soil_assessments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Farmers can view own soil assessments"
    ON public.soil_assessments FOR SELECT
    USING (auth.uid() = farmer_id);

CREATE POLICY "Farmers can insert soil assessments"
    ON public.soil_assessments FOR INSERT
    WITH CHECK (auth.uid() = farmer_id);

-- 7. MARKETPLACE PRODUCTS & ORDERS TABLE
CREATE TABLE IF NOT EXISTS public.marketplace_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    brand TEXT NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    unit TEXT NOT NULL,
    description TEXT NOT NULL,
    in_stock BOOLEAN DEFAULT TRUE,
    rating NUMERIC(3, 2) DEFAULT 4.5
);

ALTER TABLE public.marketplace_products ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read for marketplace products"
    ON public.marketplace_products FOR SELECT
    TO authenticated, anon
    USING (true);

CREATE TABLE IF NOT EXISTS public.orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL REFERENCES public.farmers(id) ON DELETE CASCADE,
    total_amount NUMERIC(10, 2) NOT NULL,
    status TEXT DEFAULT 'Processing',
    delivery_address TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_orders_farmer_id ON public.orders(farmer_id);
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Farmers can view own orders"
    ON public.orders FOR SELECT
    USING (auth.uid() = farmer_id);

CREATE POLICY "Farmers can insert orders"
    ON public.orders FOR INSERT
    WITH CHECK (auth.uid() = farmer_id);

-- 8. STORAGE BUCKET POLICIES (for crop leaf disease photos)
-- Run via Supabase SQL editor:
-- INSERT INTO storage.buckets (id, name, public) VALUES ('crop-images', 'crop-images', false);
-- CREATE POLICY "Farmers upload crop images" ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'crop-images' AND auth.uid()::text = (storage.foldername(name))[1]);
