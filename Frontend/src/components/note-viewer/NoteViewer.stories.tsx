import type { Meta, StoryObj } from '@storybook/react';
import { NoteViewer } from './NoteViewer';
import { Note } from '@/types';

const meta: Meta<typeof NoteViewer> = {
    title: 'Organisms/NoteViewer',
    component: NoteViewer,
    parameters: {
        layout: 'fullscreen',
    },
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof NoteViewer>;

const mockNote: Note = {
    id: 1,
    publicId: 'note-123',
    title: 'Introduction to React Hooks',
    subject: 'Frontend Engineering',
    department: 'CS',
    year: 'Year 4',
    status: 'PUBLISHED',
    currentVersion: 1,
    updatedAt: '2025-12-01T10:00:00Z',
    uploadedByName: 'Prof. Smith',
    content: `
# React Hooks

Hooks are functions that let you "hook into" React state and lifecycle features from function components.

## useState

\`\`\`jsx
import React, { useState } from 'react';

function Example() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>
        Click me
      </button>
    </div>
  );
}
\`\`\`

## Math Equation

The quadratic formula is:

$$
x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}
$$

> Note: Hooks rules must be followed rigorously.

![React Logo](https://upload.wikimedia.org/wikipedia/commons/a/a7/React-icon.svg)
  `
};

export const Default: Story = {
    args: {
        note: mockNote,
        onEdit: undefined
    },
};

export const WithEditAccess: Story = {
    args: {
        note: mockNote,
        onEdit: () => alert('Edit clicked')
    },
};
