import React from 'react';

type LanguageId =
  | 'typescript'
  | 'javascript'
  | 'python'
  | 'java'
  | 'kotlin'
  | 'go'
  | 'rust'
  | 'cpp'
  | 'csharp'
  | 'sql'
  | 'bash';

export interface LanguageOption {
  id: LanguageId;
  title: string;
  subtitle: string;
  accent: string;
}

export const LANGUAGE_OPTIONS: LanguageOption[] = [
  { id: 'typescript', title: 'TypeScript', subtitle: 'Web / Node.js', accent: '#3178c6' },
  { id: 'javascript', title: 'JavaScript', subtitle: 'Web / Node.js', accent: '#f7df1e' },
  { id: 'python', title: 'Python', subtitle: 'Scripts / ML', accent: '#3776ab' },
  { id: 'java', title: 'Java', subtitle: 'Spring / Android', accent: '#ea580c' },
  { id: 'kotlin', title: 'Kotlin', subtitle: 'Android / JVM', accent: '#7c3aed' },
  { id: 'go', title: 'Go', subtitle: 'Backend', accent: '#00add8' },
  { id: 'rust', title: 'Rust', subtitle: 'Systems', accent: '#b45309' },
  { id: 'cpp', title: 'C++', subtitle: 'Performance', accent: '#2563eb' },
  { id: 'csharp', title: 'C#', subtitle: '.NET', accent: '#16a34a' },
  { id: 'sql', title: 'SQL', subtitle: 'Queries', accent: '#0f766e' },
  { id: 'bash', title: 'Bash', subtitle: 'CLI', accent: '#334155' }
];

const MiniIcon: React.FC<{ label: string; accent: string }> = ({ label, accent }) => (
  <div className="lang-icon" style={{ background: accent }}>
    <span>{label}</span>
  </div>
);

const iconLabelFor = (id: LanguageId) => {
  switch (id) {
    case 'typescript':
      return 'TS';
    case 'javascript':
      return 'JS';
    case 'python':
      return 'PY';
    case 'java':
      return 'JV';
    case 'kotlin':
      return 'KT';
    case 'go':
      return 'GO';
    case 'rust':
      return 'RS';
    case 'cpp':
      return 'C++';
    case 'csharp':
      return 'C#';
    case 'sql':
      return 'SQL';
    case 'bash':
      return '>_';
    default:
      return '—';
  }
};

interface Props {
  value: string;
  onChange: (value: string) => void;
}

const LanguagePicker: React.FC<Props> = ({ value, onChange }) => {
  return (
    <div className="lang-grid" role="list">
      {LANGUAGE_OPTIONS.map((option) => {
        const selected = option.id === value;
        return (
          <button
            key={option.id}
            type="button"
            className={`lang-card ${selected ? 'selected' : ''}`}
            onClick={() => onChange(option.id)}
          >
            <MiniIcon label={iconLabelFor(option.id)} accent={option.accent} />
            <div className="lang-meta">
              <div className="lang-title">
                <span>{option.title}</span>
                {selected && <span className="pill">Выбрано</span>}
              </div>
              <div className="lang-subtitle">{option.subtitle}</div>
            </div>
          </button>
        );
      })}
    </div>
  );
};

export default LanguagePicker;

