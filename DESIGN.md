# Design System

## Product Register

Card Service is a product UI: an admin operations tool plus a customer shop demo. Design serves task completion, state inspection, and trustworthy transactional workflows.

## Color

Use a restrained operational palette.

| Token | Value | Use |
|---|---:|---|
| `--color-ink` | `#17201b` | Primary text |
| `--color-muted` | `#4f6158` | Secondary text |
| `--color-subtle` | `#6f8078` | Helper text |
| `--color-canvas` | `#f3f6f4` | App background |
| `--color-surface` | `#ffffff` | Panels, forms, tables |
| `--color-line` | `#d9e2dc` | Borders and dividers |
| `--color-brand` | `#17382e` | Primary actions and admin shell |
| `--color-brand-2` | `#245447` | Active navigation and emphasis |
| `--color-shop` | `#1f4f63` | Customer shop identity |
| `--color-success` | `#11612f` | Success states |
| `--color-warning` | `#735300` | Pending/created states |
| `--color-danger` | `#8f2d2d` | Error/refund/destructive states |

Do not use gradients, glass effects, or color as decoration. Color earns its place through action, selection, or state.

## Typography

Use the existing product font stack for now:

```css
Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif
```

Use fixed sizes, not viewport-scaled type. Product pages use compact headings, dense tables, and 12-14px labels. Keep headings at `24px` or below inside the app shell.

## Layout

- Admin uses a fixed side navigation on desktop and a compact two-column nav on small screens.
- Workspace content is constrained by stable panels, grids, and tables.
- Shop uses a full-width customer task surface with signup, status, and catalog sections.
- Avoid nested cards. Panels frame distinct tools; repeated items can use simple list or table treatments.

## Components

### Buttons

Primary buttons use the brand color. Secondary buttons use a quiet surface treatment. Danger buttons are reserved for cancellation/refund. All buttons need visible hover/focus states and stable height.

### Forms

Labels are short and bold. Inputs have consistent border, focus, padding, and disabled states. Error text appears inline and uses the same status vocabulary as notices.

### Tables

Tables prioritize scanning: compact row height, consistent dividers, muted headers, tabular numeric alignment where useful, and explicit empty/loading rows.

### Status

Status badges are compact pills. Success, warning, and danger colors map to domain states, not decoration.

### Notices

Success and error notices should reserve space naturally in the flow and use clear copy. They should not animate into view or steal focus unless they represent an error.

## Motion

Use only short interaction feedback, 150-180ms. Motion must not block forms, tables, or purchase flows. Provide a reduced-motion fallback for transitions.

## Quality Gates

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `bash scripts/hooks/validate_impeccable.sh`
