import { useEffect } from 'react'
import { Globe2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'

type Language = 'en' | 'pt' | 'pl'

const supportedLanguages: Language[] = ['en', 'pt', 'pl']

export default function LanguageSwitcher() {
  const { i18n } = useTranslation()

  const language: Language = supportedLanguages.includes(
    i18n.resolvedLanguage as Language
  )
    ? (i18n.resolvedLanguage as Language)
    : 'en'

  useEffect(() => {
    document.documentElement.lang = language
  }, [language])

  async function changeLanguage(next: Language) {
    await i18n.changeLanguage(next)

    try {
      localStorage.setItem('vpm-language', next)
    } catch {
      // O selector continua funcional sem armazenamento local.
    }
  }

  return (
    <div className="language-switcher">
      <Globe2 size={17} aria-hidden="true" />

      <select
        aria-label="Website language / Idioma do website"
        value={language}
        onChange={event =>
          void changeLanguage(event.target.value as Language)
        }
      >
        <option value="en">🇬🇧 EN</option>
        <option value="pt">🇵🇹 PT</option>
        <option value="pl">🇵🇱 PL</option>
      </select>
    </div>
  )
}
