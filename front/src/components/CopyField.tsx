import React, { useCallback, useMemo, useState } from 'react';

async function copyText(text: string) {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text);
    return;
  }
  const input = document.createElement('textarea');
  input.value = text;
  input.style.position = 'fixed';
  input.style.left = '-9999px';
  document.body.appendChild(input);
  input.select();
  document.execCommand('copy');
  document.body.removeChild(input);
}

interface Props {
  label?: string;
  value: string;
  hint?: string;
}

const CopyField: React.FC<Props> = ({ label, value, hint }) => {
  const [copied, setCopied] = useState(false);
  const formatted = useMemo(() => value.trim(), [value]);

  const handleCopy = useCallback(async () => {
    if (!formatted) return;
    try {
      await copyText(formatted);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 1200);
    } catch {
      setCopied(false);
    }
  }, [formatted]);

  return (
    <div className="copy-field">
      {label && <div className="copy-label">{label}</div>}
      <div className="copy-row">
        <input className="input" readOnly value={formatted} onFocus={(e) => e.currentTarget.select()} />
        <button type="button" className="btn btn-primary" onClick={handleCopy} disabled={!formatted}>
          {copied ? 'Скопировано' : 'Копировать'}
        </button>
      </div>
      {hint && <div className="copy-hint">{hint}</div>}
    </div>
  );
};

export default CopyField;

