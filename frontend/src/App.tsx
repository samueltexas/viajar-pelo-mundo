import AirportAutocomplete from './components/AirportAutocomplete'
import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import LanguageSwitcher from './components/LanguageSwitcher'
import {
  Plane, Globe2, UserRound, Search, CalendarDays,
  ArrowLeftRight, ShieldCheck, Headphones,
  Heart, ArrowRight, BedDouble, CarFront,
  LogOut, CreditCard, SlidersHorizontal,
  CheckCircle2, Menu, X, PlaneTakeoff,
} from 'lucide-react'
import { api, type Booking, type Credentials, type Flight, type Page } from './lib/api'
import { destinations } from './data/destinations'
import './App.css'

type User = {
  id: number
  email: string
  role: string
}

type Payment = {
  status: string
}

type View = 'home' | 'results' | 'bookings'
type TripType = 'oneway' | 'roundtrip'

const formatMoney = (value: number, currency: string, locale = 'en-GB') =>
  new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value)

const formatDate = (value: string, locale = 'en-GB') => {
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleDateString(locale, {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      })
}

function App() {
  const { t, i18n } = useTranslation()
  const locale =
    i18n.resolvedLanguage === 'pl'
      ? 'pl-PL'
      : i18n.resolvedLanguage === 'pt'
        ? 'pt-PT'
        : 'en-GB'
  const [view, setView] = useState<View>('home')
  const [mobileMenu, setMobileMenu] = useState(false)
  const [credentials, setCredentials] = useState<Credentials | null>(null)
  const [user, setUser] = useState<User | null>(null)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [registerMode, setRegisterMode] = useState(false)

  const [origin, setOrigin] = useState('WAW')
  const [destination, setDestination] = useState('NBJ')
  const [departure, setDeparture] = useState('2026-12-15')
  const [returnDate, setReturnDate] = useState('')
  const [passengers, setPassengers] = useState(1)
  const [tripType, setTripType] = useState<TripType>('oneway')

  const [flights, setFlights] = useState<Flight[]>([])
  const [bookings, setBookings] = useState<Booking[]>([])
  const [selectedBooking, setSelectedBooking] = useState<string | null>(null)
  const [paymentStatuses, setPaymentStatuses] = useState<Record<string, string>>({})
  const [directOnly, setDirectOnly] = useState(false)
  const [sort, setSort] = useState('price')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  async function authenticate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)
    setMessage('')

    const auth = { email: email.trim(), password }

    try {
      if (registerMode) {
        await api<User>('/auth/register', {
          method: 'POST',
          body: JSON.stringify(auth),
        })
      }

      const current = await api<User>('/auth/me', {}, auth)
      setCredentials(auth)
      setUser(current)
      setPassword('')
      setMessage(t("messages.welcome", { email: current.email }))
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function searchFlights(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault()
    setLoading(true)
    setMessage('')

    try {
      const query = new URLSearchParams({
        origin: origin.trim().toUpperCase(),
        destination: destination.trim().toUpperCase(),
        date: departure,
        passengers: String(passengers),
        size: '50',
      })

      const result = await api<Page<Flight>>(`/flights?${query}`)
      setFlights(result.content)
      setView('results')

      if (tripType === 'roundtrip') {
        setMessage(t("messages.returnPreview"))
      } else {
        setMessage(t("messages.flightsFound", { count: result.totalElements }))
      }
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function loadBookings(auth = credentials) {
    if (!auth) return

    try {
      const result = await api<Page<Booking>>(
        '/bookings/mine?page=0&size=50',
        {},
        auth,
      )
      setBookings(result.content)
    } catch (error) {
      setMessage((error as Error).message)
    }
  }

  async function openBookings() {
    setMobileMenu(false)
    if (!credentials) {
      setMessage(t("messages.signInTrips"))
      setView('home')
      return
    }
    setView('bookings')
    await loadBookings()
  }

  async function bookFlight(flightId: number) {
    if (!credentials) {
      setMessage(t("messages.signInFirst"))
      setView('home')
      return
    }

    setLoading(true)
    setMessage('')

    try {
      const booking = await api<Booking>(
        '/bookings',
        {
          method: 'POST',
          body: JSON.stringify({ flightId, passengers }),
        },
        credentials,
      )

      setMessage(t("messages.bookingCreated", { reference: booking.reference }))
      await loadBookings()
      setView('bookings')
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function bookingAction(reference: string, action: 'pay' | 'cancel') {
    if (!credentials) return

    setLoading(true)
    setMessage('')

    try {
      if (action === 'pay') {
        const payment = await api<Payment>(
          `/bookings/${reference}/pay`,
          { method: 'POST' },
          credentials,
        )
        setPaymentStatuses(current => ({
          ...current,
          [reference]: payment.status,
        }))
        setMessage(t("messages.payment", { status: payment.status }))
      } else {
        await api<Booking>(
          `/bookings/${reference}/cancel`,
          { method: 'POST' },
          credentials,
        )
        setMessage(t("messages.cancelled"))
      }

      await loadBookings()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function checkPayment(reference: string) {
    if (!credentials) return

    try {
      const payment = await api<Payment>(
        `/bookings/${reference}/payment`,
        {},
        credentials,
      )
      setPaymentStatuses(current => ({
        ...current,
        [reference]: payment.status,
      }))
      setSelectedBooking(reference)
    } catch (error) {
      setMessage((error as Error).message)
    }
  }

  function logout() {
    setCredentials(null)
    setUser(null)
    setBookings([])
    setPaymentStatuses({})
    setView('home')
    setMessage(t("messages.logout"))
  }

  function selectDestination(code: string) {
    setDestination(code)
    setView('home')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const sortedFlights = [...flights]
    .sort((a, b) => sort === 'price'
      ? a.basePrice - b.basePrice
      : new Date(a.departureTime).getTime() -
        new Date(b.departureTime).getTime())

  return (
    <div className="site">
      <header className="site-header">
        <div className="header-inner">
          <button className="brand" onClick={() => setView('home')}>
            <span className="brand-symbol"><Globe2 size={30}/><Plane size={16}/></span>
            <span>ViajarPelo<strong>Mundo</strong></span>
          </button>

          <nav className={mobileMenu ? 'navigation open' : 'navigation'}>
            <button
              className={view === 'home' || view === 'results' ? 'active' : ''}
              onClick={() => { setView('home'); setMobileMenu(false) }}
            >
              {t("nav.flights")}
            </button>
            <button
              onClick={() => {
                setView('home')
                setMobileMenu(false)
                document.getElementById('destinations')?.scrollIntoView({ behavior: 'smooth' })
              }}
            >
              {t("nav.destinations")}
            </button>
            <button className={view === 'bookings' ? 'active' : ''} onClick={openBookings}>
              {t("nav.trips")}
            </button>
            <button onClick={() => setMessage(t("messages.help"))}>
              {t("nav.help")}
            </button>
          </nav>

          <div className="header-actions">
            <LanguageSwitcher/>
            <button
              className="avatar"
              title={user ? user.email : 'Sign in'}
              onClick={() => user ? void openBookings() : document.getElementById('account')?.scrollIntoView({ behavior: 'smooth' })}
            >
              <UserRound size={20}/>
            </button>
            <button className="mobile-toggle" onClick={() => setMobileMenu(!mobileMenu)}>
              {mobileMenu ? <X/> : <Menu/>}
            </button>
          </div>
        </div>
      </header>

      {view === 'home' && (
        <>
          <section className="hero">
            <div className="hero-overlay"/>
            <div className="hero-inner">
              <span className="hero-kicker">{t("hero.kicker")}</span>
              <h1>{t("hero.title")}<br/><em>{t("hero.highlight")}</em></h1>
              <p>{t("hero.description")}</p>
            </div>
          </section>

          <div className="search-wrap" id="search">
            <div className="search-tabs">
              <button className="selected"><PlaneTakeoff size={18}/> {t("search.flights")}</button>
              <button disabled title="Coming soon"><BedDouble size={18}/> {t("search.flightHotel")}</button>
              <button disabled title="Coming soon"><BedDouble size={18}/> {t("search.stays")}</button>
              <button disabled title="Coming soon"><CarFront size={18}/> {t("search.cars")}</button>
            </div>

            <form className="search-box" onSubmit={searchFlights}>
              <div className="trip-types">
                <label>
                  <input
                    type="radio"
                    checked={tripType === 'oneway'}
                    onChange={() => setTripType('oneway')}
                  />
                  {t("search.oneWay")}
                </label>
                <label>
                  <input
                    type="radio"
                    checked={tripType === 'roundtrip'}
                    onChange={() => setTripType('roundtrip')}
                  />
                  {t("search.roundTrip")} <small>{t("search.preview")}</small>
                </label>
              </div>

              <div className="search-fields">
                <div className="search-field airport-field">
                  <AirportAutocomplete
                    label={t("search.from")}
                    value={origin}
                    onChange={setOrigin}
                    excludeCode={destination}
                  />
                </div>

                <button
                  className="swap-button"
                  type="button"
                  aria-label={t("search.swap")}
                  onClick={() => {
                    setOrigin(destination)
                    setDestination(origin)
                  }}
                >
                  <ArrowLeftRight size={19}/>
                </button>

                <div className="search-field airport-field">
                  <AirportAutocomplete
                    label={t("search.to")}
                    value={destination}
                    onChange={setDestination}
                    excludeCode={origin}
                  />
                </div>

                <label className="search-field">
                  <span>{t("search.departure")}</span>
                  <div><CalendarDays size={17}/><input type="date" value={departure} onChange={e => setDeparture(e.target.value)} required/></div>
                </label>

                {tripType === 'roundtrip' && (
                  <label className="search-field">
                    <span>{t("search.return")} {t("search.preview")}</span>
                    <div><CalendarDays size={17}/><input type="date" min={departure} value={returnDate} onChange={e => setReturnDate(e.target.value)}/></div>
                  </label>
                )}

                <label className="search-field passenger-field">
                  <span>{t("search.passengers")}</span>
                  <div>
                    <UserRound size={17}/>
                    <select value={passengers} onChange={e => setPassengers(Number(e.target.value))}>
                      {Array.from({ length: 9 }, (_, i) => <option key={i+1} value={i+1}>{i+1} {i === 0 ? t("search.adult") : t("search.passengers").toLowerCase()}</option>)}
                    </select>
                  </div>
                </label>

                <button className="gold-button" disabled={loading} type="submit">
                  <Search size={18}/>{loading ? t("search.searching") : t("search.submit")}
                </button>
              </div>
            </form>
          </div>

          <section className="benefits container">
            <div><Plane size={27}/><span><strong>{t("benefits.routes")}</strong><small>{t("benefits.routesDescription")}</small></span></div>
            <div><ShieldCheck size={27}/><span><strong>{t("benefits.accounts")}</strong><small>{t("benefits.accountsDescription")}</small></span></div>
            <div><CalendarDays size={27}/><span><strong>{t("benefits.management")}</strong><small>{t("benefits.managementDescription")}</small></span></div>
            <div><Headphones size={27}/><span><strong>{t("benefits.support")}</strong><small>{t("benefits.comingSoon")}</small></span></div>
          </section>

          <section className="destinations-section container" id="destinations">
            <div className="section-title">
              <div>
                <span className="section-kicker">{t("destinations.kicker")}</span>
                <h2>{t("destinations.title")}</h2>
              </div>
              <span className="muted-link">{t("destinations.more")} <ArrowRight size={16}/></span>
            </div>

            <div className="destinations-grid">
              {destinations.map(place => (
                <article className="destination-card" key={place.code}>
                  <img src={place.image} alt={`Travel destination: ${place.city}`} loading="lazy"/>
                  <div className="destination-shade"/>
                  <button className="favorite" aria-label={t("destinations.save", { city: place.city })} onClick={() => setMessage(t("messages.favorites"))}>
                    <Heart size={20}/>
                  </button>
                  <div className="destination-info">
                    <h3>{t(`destinations.city.${place.city}`)}</h3>
                    <p>{t(`destinations.country.${place.country}`)}</p>
                    <div className="destination-bottom">
                      <span>{t("destinations.explore")}</span>
                      <button onClick={() => selectDestination(place.code)} aria-label={t("destinations.searchTo", { city: place.city })}>
                        <ArrowRight size={19}/>
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </section>

          {!user && (
            <section className="account-section container" id="account">
              <div className="account-copy">
                <span className="section-kicker">{t("account.kicker")}</span>
                <h2>{registerMode ? t("account.registerTitle") : t("account.welcome")}</h2>
                <p>{t("account.description")}</p>
                <div className="account-benefit"><CheckCircle2 size={19}/> {t("account.benefitBookings")}</div>
                <div className="account-benefit"><CheckCircle2 size={19}/> {t("account.benefitPayments")}</div>
              </div>
              <form className="account-form" onSubmit={authenticate}>
                <label>{t("account.email")}<input type="email" value={email} onChange={e => setEmail(e.target.value)} required autoComplete="email"/></label>
                <label>{t("account.password")}<input type="password" value={password} onChange={e => setPassword(e.target.value)} minLength={registerMode ? 12 : undefined} required autoComplete={registerMode ? 'new-password' : 'current-password'}/></label>
                <button className="navy-button" disabled={loading}>{registerMode ? t("account.register") : t("account.signIn")}</button>
                <button className="link-button" type="button" onClick={() => setRegisterMode(!registerMode)}>
                  {registerMode ? t("account.existing") : t("account.new")}
                </button>
              </form>
            </section>
          )}
        </>
      )}

      {view === 'results' && (
        <main className="container results-page">
          <button className="back-link" onClick={() => setView('home')}>← {t("results.newSearch")}</button>
          <h1>{t("results.heading", { origin: origin.toUpperCase(), destination: destination.toUpperCase() })}</h1>
          <p className="results-subtitle">{t("results.subtitle", { date: formatDate(departure, locale), count: passengers })}</p>

          <div className="results-layout">
            <aside className="filter-panel">
              <h3><SlidersHorizontal size={18}/> {t("results.filters")}</h3>
              <p>{t("results.filterDescription")}</p>
              <label>{t("results.sort")}
                <select value={sort} onChange={e => setSort(e.target.value)}>
                  <option value="price">{t("results.lowestPrice")}</option>
                  <option value="departure">{t("results.earliest")}</option>
                </select>
              </label>
              <label className="check-filter">
                <input type="checkbox" checked={directOnly} onChange={e => setDirectOnly(e.target.checked)} disabled/>
                {t("results.direct")}
              </label>
            </aside>

            <section className="flight-results">
              <div className="results-heading">
                <h2>{t("results.available", { count: flights.length })}</h2>
                <span>{t("results.pricePerPassenger")}</span>
              </div>

              {sortedFlights.length === 0 && (
                <div className="empty-card">{t("results.empty")}</div>
              )}

              {sortedFlights.map(flight => (
                <article className="result-card" key={flight.id}>
                  <div className="airline-details">
                    <span className="airline-badge"><Plane size={23}/></span>
                    <div><strong>{flight.airlineName || t("results.partnerAirline")}</strong><small>{flight.flightNumber}</small></div>
                  </div>
                  <div className="flight-route">
                    <div><strong>{new Date(flight.departureTime).toLocaleTimeString(locale, {hour:'2-digit',minute:'2-digit'})}</strong><small>{flight.origin}</small></div>
                    <div className="route-line"><Plane size={18}/><span>{t("results.flight")}</span></div>
                    <div><strong>{new Date(flight.arrivalTime).toLocaleTimeString(locale, {hour:'2-digit',minute:'2-digit'})}</strong><small>{flight.destination}</small></div>
                  </div>
                  <div className="result-price">
                    <strong>{formatMoney(flight.basePrice, flight.currency, locale)}</strong>
                    <small>{t("results.seats", { count: flight.availableSeats })}</small>
                    <button className="gold-button" disabled={loading} onClick={() => void bookFlight(flight.id)}>{t("results.select")}</button>
                  </div>
                </article>
              ))}
            </section>
          </div>
        </main>
      )}

      {view === 'bookings' && (
        <main className="container trips-page">
          <aside className="profile-sidebar">
            <div className="profile-user"><span className="profile-avatar"><UserRound/></span><div><strong>{t("trips.account")}</strong><small>{user?.email}</small></div></div>
            <button className="selected"><CalendarDays size={17}/> {t("trips.title")}</button>
            <button onClick={() => setMessage(t("messages.profile"))}><UserRound size={17}/> {t("trips.profile")}</button>
            <button onClick={() => setMessage(t("messages.payments"))}><CreditCard size={17}/> {t("trips.payments")}</button>
            <button onClick={logout}><LogOut size={17}/> {t("trips.logout")}</button>
          </aside>

          <section className="trips-content">
            <span className="section-kicker">{t("trips.kicker")}</span>
            <h1>{t("trips.title")}</h1>
            <p>{t("trips.description")}</p>

            <div className="trip-tabs">
              <span>{t("trips.all", { count: bookings.length })}</span>
            </div>

            {bookings.length === 0 && <div className="empty-card">{t("trips.empty")}</div>}

            {bookings.map(booking => (
              <article className="trip-card" key={booking.reference}>
                <div className="trip-top">
                  <span className="trip-plane"><Plane size={24}/></span>
                  <div className="trip-details">
                    <h3>{booking.flightNumber}</h3>
                    <small>{t("trips.reference", { reference: booking.reference })}</small>
                    <p><UserRound size={15}/> {t("trips.passengers", { count: booking.passengers })} <span>·</span> {formatMoney(booking.totalPrice, booking.currency, locale)}</p>
                  </div>
                  <span className={`status-pill ${booking.status.toLowerCase()}`}>{t(`trips.status.${booking.status}`, { defaultValue: booking.status })}</span>
                </div>

                {selectedBooking === booking.reference && (
                  <p className="payment-detail">
                    {t("trips.paymentDetail", { status: paymentStatuses[booking.reference] || t("trips.unknown") })}
                  </p>
                )}

                <div className="trip-actions">
                  <button className="outline-button" onClick={() => void checkPayment(booking.reference)}>
                    <CreditCard size={16}/> {t("trips.paymentStatus")}
                  </button>
                  {booking.status === 'CONFIRMED' && (
                    <>
                      {paymentStatuses[booking.reference] !== 'PAID' && (
                        <button className="navy-button" disabled={loading} onClick={() => void bookingAction(booking.reference, 'pay')}>
                          {t("trips.simulate")}
                        </button>
                      )}
                      <button className="outline-button danger" disabled={loading} onClick={() => void bookingAction(booking.reference, 'cancel')}>
                        {t("trips.cancel")}
                      </button>
                    </>
                  )}
                </div>
              </article>
            ))}
          </section>
        </main>
      )}

      {message && (
        <div className="toast" role="status">
          <span>{message}</span>
          <button onClick={() => setMessage('')} aria-label="Dismiss message"><X size={17}/></button>
        </div>
      )}

      <footer className="site-footer">
        <div className="container footer-inner">
          <div><strong>ViajarPelo<span>Mundo</span></strong><p>{t("footer.tagline")}</p></div>
          <div><p>Java · Spring Boot · PostgreSQL · React · TypeScript</p><small>{t("footer.disclaimer")}</small></div>
        </div>
      </footer>
    </div>
  )
}

export default App
