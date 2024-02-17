-- Create a table for jobs
CREATE TABLE jobs(
  id UUID DEFAULT gen_random_uuid(),
  timestamp TIMESTAMP NOT NUll,
  owner_email TEXT NOT NULL,
  company TEXT NOT NULL,
  title TEXT NOT NULL,
  description TEXT NOT NULL,
  external_url TEXT NOT NULL,
  location TEXT,
  remote BOOLEAN NOT NULL DEFAULT false,
  salary INTEGER,
  currency TEXT,
  country TEXT,
  tags TEXT [],
  image TEXT,
  seniority TEXT,
  other TEXT,
  active BOOLEAN NOT NULL DEFAULT false
);
-- Add a primary key
ALTER TABLE jobs
ADD CONSTRAINT pk_jobs PRIMARY KEY (id);
-- Insert a job
INSERT INTO jobs (
    id,
    timestamp,
    owner_email,
    company,
    title,
    description,
    external_url,
    location,
    remote,
    salary,
    currency,
    country,
    tags,
    image,
    seniority,
    other,
    active
  )
VALUES (
    '843df718-ec6e-4d49-9289-f799c0f40064',
    '2024-01-01T00:00:00Z',
    'test@test.com',
    'Awesome Company',
    'Tech Lead',
    'An awesome job in Berlin',
    'https://rockthejvm.com/awesomejob',
    'Berlin',
    false,
    5000,
    'EUR',
    'Germany',
    ARRAY [ 'scala', 'scala-3', 'cats' ],
    NULL,
    'Senior',
    NULL,
    false
  )