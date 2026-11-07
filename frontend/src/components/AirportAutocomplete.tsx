import { useEffect, useId, useMemo, useState } from 'react'
import { MapPin, Plane } from 'lucide-react'
import { api } from '../lib/api'
import { useTranslation } from 'react-i18next'

type Airport = {
  id: number
  iataCode: string
  icaoCode: string | null
  name: string
  city: string
  countryCode: string
}

type Props = {
  label: string
  value: string
  onChange: (code: string) => void
  excludeCode?: string
}

let cachedAirports: Airport[] | null = null
let pendingRequest: Promise<Airport[]> | null = null

function loadAirports(): Promise<Airport[]> {
  if (cachedAirports) return Promise.resolve(cachedAirports)

  if (!pendingRequest) {
    pendingRequest = api<Airport[]>('/airports')
      .then(data => {
        cachedAirports = data
        return data
      })
      .finally(() => {
        pendingRequest = null
      })
  }

  return pendingRequest
}

export default function AirportAutocomplete({
  label,
  value,
  onChange,
  excludeCode,
}: Props) {
  const { i18n, t } = useTranslation()
  const listId = useId()
  const [airports, setAirports] = useState<Airport[]>(cachedAirports ?? [])
  const [query, setQuery] = useState(value)
  const [open, setOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(0)
  const [error, setError] = useState(false)

  const locale = i18n.resolvedLanguage === 'pt'
    ? 'pt-PT'
    : i18n.resolvedLanguage === 'pl'
      ? 'pl-PL'
      : 'en-GB'

  useEffect(() => {
    let mounted = true

    loadAirports()
      .then(data => {
        if (mounted) setAirports(data)
      })
      .catch(() => {
        if (mounted) setError(true)
      })

    return () => {
      mounted = false
    }
  }, [])

  useEffect(() => {
    if (!open) setQuery(value)
  }, [value, open])

  const getCountry = (code: string) => {
    try {
      return new Intl.DisplayNames([locale], { type: 'region' }).of(code) ?? code
    } catch {
      return code
    }
  }

  const filtered = useMemo(() => {
    const term = query.trim().toLocaleLowerCase()

    return airports
      .filter(airport => airport.iataCode !== excludeCode)
      .filter(airport => {
        if (!term) return true

        return [
          airport.iataCode,
          airport.icaoCode ?? '',
          airport.name,
          airport.city,
          airport.countryCode,
          getCountry(airport.countryCode),
        ].some(field => field.toLocaleLowerCase().includes(term))
      })
      .sort((a, b) => {
        const aExact = a.iataCode.toLowerCase() === term ? 0 : 1
        const bExact = b.iataCode.toLowerCase() === term ? 0 : 1
        return aExact - bExact || a.city.localeCompare(b.city)
      })
      .slice(0, 8)
  }, [airports, query, excludeCode, locale])

  function selectAirport(airport: Airport) {
    onChange(airport.iataCode)
    setQuery(airport.iataCode)
    setOpen(false)
    setActiveIndex(0)
  }

  return (
    <div className="airport-autocomplete">
      <label className="airport-label" htmlFor={listId}>{label}</label>

      <div className="airport-input-row">
        <MapPin size={17} aria-hidden="true" />
        <input
          id={listId}
          value={query}
          autoComplete="off"
          role="combobox"
          aria-expanded={open}
          aria-controls={`${listId}-options`}
          aria-autocomplete="list"
          aria-activedescendant={
            open && filtered[activeIndex]
              ? `${listId}-option-${activeIndex}`
              : undefined
          }
          placeholder={t("airports.placeholder")}
          required
          onFocus={() => {
            setQuery('')
            setOpen(true)
            setActiveIndex(0)
          }}
          onChange={event => {
            const next = event.target.value
            setQuery(next)
            onChange('')
            setOpen(true)
            setActiveIndex(0)
          }}
          onBlur={() => {
            setOpen(false)
            setQuery(value)
          }}
          onKeyDown={event => {
            if (event.key === 'Escape') {
              setOpen(false)
              setQuery(value)
            }

            if (event.key === 'ArrowDown' && open) {
              event.preventDefault()
              setActiveIndex(index => Math.min(index + 1, filtered.length - 1))
            }

            if (event.key === 'ArrowUp' && open) {
              event.preventDefault()
              setActiveIndex(index => Math.max(index - 1, 0))
            }

            if (event.key === 'Enter' && open && filtered[activeIndex]) {
              event.preventDefault()
              selectAirport(filtered[activeIndex])
            }
          }}
        />
      </div>

      {open && (
        <div className="airport-dropdown" id={`${listId}-options`} role="listbox">
          {error && (
            <div className="airport-empty">
              {t("airports.loading")}
            </div>
          )}

          {!error && filtered.length === 0 && (
            <div className="airport-empty">
              {t("airports.empty")}
            </div>
          )}

          {filtered.map((airport, index) => (
            <button
              type="button"
              role="option"
              aria-selected={index === activeIndex}
              id={`${listId}-option-${index}`}
              className={index === activeIndex ? 'airport-option active' : 'airport-option'}
              key={airport.iataCode}
              onMouseDown={event => event.preventDefault()}
              onClick={() => selectAirport(airport)}
            >
              <Plane size={17} aria-hidden="true" />

              <span className="airport-option-info">
                <strong>
                  {airport.city}, {getCountry(airport.countryCode)}
                </strong>
                <small>{airport.name}</small>
              </span>

              <span className="airport-iata">{airport.iataCode}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
