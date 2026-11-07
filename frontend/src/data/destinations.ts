export type Destination = {
  city: string
  country: string
  code: string
  price: number | null
  image: string
}

export const destinations: Destination[] = [
  {
    city: 'Dubai',
    country: 'United Arab Emirates',
    code: 'DXB',
    price: null,
    image: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=900&q=85',
  },
  {
    city: 'Lisbon',
    country: 'Portugal',
    code: 'LIS',
    price: null,
    image: 'https://images.unsplash.com/photo-1555881400-74d7acaacd8b?auto=format&fit=crop&w=900&q=85',
  },
  {
    city: 'Luanda',
    country: 'Angola',
    code: 'NBJ',
    price: null,
    image: 'https://images.unsplash.com/photo-1480796927426-f609979314bd?auto=format&fit=crop&w=900&q=85',
  },
  {
    city: 'Paris',
    country: 'France',
    code: 'CDG',
    price: null,
    image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=900&q=85',
  },
]
