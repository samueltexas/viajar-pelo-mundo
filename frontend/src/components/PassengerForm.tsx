import type { PassengerDetails } from '../lib/api'

type Props = {
  passengers: PassengerDetails[]
  onChange: (passengers: PassengerDetails[]) => void
  language: string
}

const labels = {
  en: {
    passenger: 'Passenger',
    firstName: 'First name',
    lastName: 'Last name',
    birth: 'Date of birth',
  },
  pt: {
    passenger: 'Passageiro',
    firstName: 'Nome',
    lastName: 'Apelido',
    birth: 'Data de nascimento',
  },
  pl: {
    passenger: 'Pasażer',
    firstName: 'Imię',
    lastName: 'Nazwisko',
    birth: 'Data urodzenia',
  },
}

export default function PassengerForm({
  passengers,
  onChange,
  language,
}: Props) {
  const locale = language.startsWith('pt')
    ? 'pt'
    : language.startsWith('pl')
      ? 'pl'
      : 'en'

  const t = labels[locale]

  function update(
    index: number,
    field: keyof PassengerDetails,
    value: string,
  ) {
    onChange(
      passengers.map((passenger, i) =>
        i === index
          ? { ...passenger, [field]: value }
          : passenger
      )
    )
  }

  return (
    <div className="vpm-passenger-form">
      {passengers.map((passenger, index) => (
        <fieldset className="vpm-passenger-card" key={index}>
          <legend>{t.passenger} {index + 1}</legend>

          <div className="vpm-passenger-grid">
            <label>
              {t.firstName}
              <input
                required
                maxLength={100}
                autoComplete="given-name"
                value={passenger.firstName}
                onChange={e =>
                  update(index, 'firstName', e.target.value)
                }
              />
            </label>

            <label>
              {t.lastName}
              <input
                required
                maxLength={100}
                autoComplete="family-name"
                value={passenger.lastName}
                onChange={e =>
                  update(index, 'lastName', e.target.value)
                }
              />
            </label>

            <label>
              {t.birth}
              <input
                required
                type="date"
                max={new Date().toLocaleDateString('en-CA')}
                value={passenger.dateOfBirth}
                onChange={e =>
                  update(index, 'dateOfBirth', e.target.value)
                }
              />
            </label>
          </div>
        </fieldset>
      ))}
    </div>
  )
}
