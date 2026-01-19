import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import '@testing-library/jest-dom';
import { NoteViewer } from './NoteViewer';
import { Note } from '@/types';

// Mock ReactMarkdown to avoid complex parsing in unit tests
// But strictly, we might want to test it renders. 
// For unit tests, we can keep it real or shallow. Let's keep it real but simple content.

const mockNote: Note = {
    id: 1,
    publicId: '123',
    title: 'Test Note',
    content: '# Hello World\nThis is a test.',
    department: 'CS',
    year: 'Y1',
    subject: 'Math',
    status: 'PUBLISHED',
    currentVersion: 1,
    updatedAt: '2025-01-01T12:00:00Z',
    uploadedByName: 'Tester'
};

describe('NoteViewer', () => {
    it('renders note title and content', () => {
        render(<NoteViewer note={mockNote} />);
        expect(screen.getByText('Test Note')).toBeInTheDocument();
        expect(screen.getByText('Hello World', { exact: false })).toBeInTheDocument(); // Markdown heading
    });

    it('shows edit button when onEdit is provided', () => {
        const onEdit = vi.fn();
        render(<NoteViewer note={mockNote} onEdit={onEdit} />);
        const fab = screen.getByLabelText('Edit Note');
        expect(fab).toBeInTheDocument();
        fireEvent.click(fab);
        expect(onEdit).toHaveBeenCalled();
    });

    it('does not show edit button when onEdit is missing', () => {
        render(<NoteViewer note={mockNote} />);
        expect(screen.queryByLabelText('Edit Note')).not.toBeInTheDocument();
    });
});
