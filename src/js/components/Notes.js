import { config } from '../config.js';
import { apiService } from '../utils/apiService.js';

export class Notes {
  constructor() {
    this.state = {
      notes: [],
      query: "",
      category: "all",
      sort: "updated",
    };
    
    this.els = null;
    this.initialized = false;
  }

  initialize() {
    if (this.initialized) return;
    
    this.els = {
      grid: document.getElementById("notes-grid"),
      search: document.getElementById("search-input"),
      newBtn: document.getElementById("new-note-btn"),
      catFilter: document.getElementById("category-filter"),
      sortFilter: document.getElementById("sort-filter"),
      overlay: document.getElementById("editor-overlay"),
      dialog: document.getElementById("editor-dialog"),
      form: document.getElementById("note-form"),
      id: document.getElementById("note-id"),
      title: document.getElementById("note-title"),
      content: document.getElementById("note-content"),
      tags: document.getElementById("note-tags"),
      category: document.getElementById("note-category"),
      pinned: document.getElementById("note-pinned"),
      cancel: document.getElementById("cancel-edit"),
      template: document.getElementById("note-card-template"),
    };

    if (this.els.grid) {
      this.bindEvents();
      this.load();
      this.initialized = true;
    }
  }

  // Load notes from backend API
  async load() {
    try {
      const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.notes}`, {
        headers: apiService.getHeaders()
      });
      
      if (response.ok) {
        this.state.notes = await response.json();
      } else {
        console.error('Failed to load notes:', response.statusText);
        this.state.notes = [];
      }
    } catch (error) {
      console.error('Error loading notes:', error);
      this.state.notes = [];
    }
    this.render();
  }

  // Save note to backend API
  async saveToBackend(note) {
    try {
      const isEdit = !!note.id;
      const url = isEdit 
        ? `${apiService.baseUrl}${config.api.endpoints.notes}/${note.id}`
        : `${apiService.baseUrl}${config.api.endpoints.notes}`;
      
      const method = isEdit ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: apiService.getHeaders(),
        body: JSON.stringify(note)
      });
      
      if (!response.ok) {
        throw new Error(`Failed to save note: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error saving note:', error);
      throw error;
    }
  }

  // Delete note from backend API
  async deleteFromBackend(id) {
    try {
      const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.notes}/${id}`, {
        method: 'DELETE',
        headers: apiService.getHeaders()
      });
      
      if (!response.ok) {
        throw new Error(`Failed to delete note: ${response.statusText}`);
      }
    } catch (error) {
      console.error('Error deleting note:', error);
      throw error;
    }
  }

  timeAgo(ts) {
    // Handle both Date objects and timestamp numbers
    const time = typeof ts === 'string' ? new Date(ts).getTime() : ts;
    const s = Math.floor((Date.now() - time) / 1000)
    if (s < 60) return `${s}s ago`
    const m = Math.floor(s / 60)
    if (m < 60) return `${m}m ago`
    const h = Math.floor(m / 60)
    if (h < 24) return `${h}h ago`
    const d = Math.floor(h / 24)
    if (d < 30) return `${d}d ago`
    const mo = Math.floor(d / 30)
    if (mo < 12) return `${mo}mo ago`
    const y = Math.floor(mo / 12)
    return `${y}y ago`
  }

  parseTags(str) {
    return str
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean)
      .map((t) => t.toLowerCase())
  }

  openEditor(data = null) {
    this.els.overlay.hidden = false
    this.els.dialog.hidden = false
    document.body.style.overflow = "hidden"

    if (data) {
      this.els.id.value = data.id
      this.els.title.value = data.title || ""
      this.els.content.value = data.content || ""
      
      // Handle tags - both array and comma-separated string
      const tagsArray = Array.isArray(data.tags) ? data.tags : (data.tags || '').split(',').map(t => t.trim()).filter(Boolean);
      this.els.tags.value = tagsArray.join(", ")
      
      // Handle category - backend returns uppercase
      this.els.category.value = data.category ? data.category.toUpperCase() : "IDEAS"
      this.els.pinned.checked = !!data.pinned
      document.getElementById("editor-title").textContent = "Edit note"
    } else {
      this.els.id.value = ""
      this.els.title.value = ""
      this.els.content.value = ""
      this.els.tags.value = ""
      this.els.category.value = "IDEAS"
      this.els.pinned.checked = false
      document.getElementById("editor-title").textContent = "New note"
    }

    // Move focus into dialog
    window.setTimeout(() => this.els.title.focus(), 0)
  }

  closeEditor() {
    this.els.overlay.hidden = true
    this.els.dialog.hidden = true
    document.body.style.overflow = ""
    this.els.form.reset()
    this.els.id.value = ""
  }

  async upsertNote(evt) {
    evt.preventDefault()

    const id = this.els.id.value || null;
    const note = {
      title: this.els.title.value.trim() || "Untitled",
      content: this.els.content.value.trim(),
      tags: this.parseTags(this.els.tags.value).join(', '), // Backend expects comma-separated string
      category: this.els.category.value.toUpperCase(), // Backend expects uppercase
      pinned: !!this.els.pinned.checked,
    }

    if (id) {
      note.id = parseInt(id); // Backend expects integer ID
    }

    try {
      const savedNote = await this.saveToBackend(note);
      await this.load(); // Reload all notes from backend
      this.closeEditor();
    } catch (error) {
      alert(`Failed to save note: ${error.message}`);
    }
  }

  async deleteNote(id) {
    if (!confirm('Delete this note?')) return;
    
    console.log('Deleting note:', id);
    
    try {
      await this.deleteFromBackend(id);
      console.log('Note deleted successfully');
      await this.load(); // Reload all notes from backend
    } catch (error) {
      console.error('Failed to delete note:', error);
      alert(`Failed to delete note: ${error.message}`);
    }
  }

  async togglePin(id) {
    const n = this.state.notes.find((x) => x.id === id)
    if (!n) return
    
    const updatedNote = {
      ...n,
      pinned: !n.pinned,
      tags: Array.isArray(n.tags) ? n.tags.join(', ') : n.tags, // Ensure tags is string
      category: n.category.toUpperCase() // Ensure uppercase
    };
    
    try {
      await this.saveToBackend(updatedNote);
      await this.load(); // Reload all notes from backend
    } catch (error) {
      alert(`Failed to update note: ${error.message}`);
    }
  }

  bySort(a, b) {
    // Handle both string timestamps and Date objects
    const getTime = (val) => {
      if (!val) return 0;
      return typeof val === 'string' ? new Date(val).getTime() : val;
    };
    
    if (this.state.sort === "updated") return getTime(b.updatedAt) - getTime(a.updatedAt)
    if (this.state.sort === "created") return getTime(b.createdAt) - getTime(a.createdAt)
    // alpha
    return a.title.localeCompare(b.title)
  }

  filterNotes() {
    const q = this.state.query.toLowerCase().trim()
    return this.state.notes
      .filter((n) => {
        if (this.state.category === "all") return true;
        // Backend returns uppercase categories
        return n.category && n.category.toUpperCase() === this.state.category.toUpperCase();
      })
      .filter((n) => {
        if (!q) return true
        const tags = Array.isArray(n.tags) ? n.tags : (n.tags || '').split(',').map(t => t.trim());
        return (
          n.title.toLowerCase().includes(q) ||
          n.content.toLowerCase().includes(q) ||
          tags.some((t) => t.toLowerCase().includes(q))
        )
      })
  }

  render() {
    this.els.grid.setAttribute("aria-busy", "true")
    const notes = this.filterNotes().sort((a, b) => {
      // Pinned first, then sort selection
      if (a.pinned !== b.pinned) return a.pinned ? -1 : 1
      return this.bySort(a, b)
    })

    this.els.grid.innerHTML = ""

    if (notes.length === 0) {
      const empty = document.createElement("div")
      empty.setAttribute("role", "status")
      empty.className = "empty"
      empty.innerHTML = `
        <div class="note-card">
          <h3 class="note-title">No notes found</h3>
          <p class="note-content">Try adjusting your search or filters, or create a new note.</p>
          <div class="note-actions">
            <button class="btn primary" id="empty-create">Create note</button>
          </div>
        </div>
      `
      this.els.grid.appendChild(empty)
      empty.querySelector("#empty-create").addEventListener("click", () => this.openEditor())
      this.els.grid.setAttribute("aria-busy", "false")
      return
    }

    for (const n of notes) {
      const node = this.els.template.content.firstElementChild.cloneNode(true)
      node.dataset.id = n.id

      // Badge - handle uppercase category from backend
      const categoryDisplay = n.category ? n.category.charAt(0).toUpperCase() + n.category.slice(1).toLowerCase() : 'Ideas';
      node.querySelector("[data-badge='category']").textContent = categoryDisplay

      // Pin button state
      const pinBtn = node.querySelector(".icon-btn.pin")
      pinBtn.setAttribute("aria-pressed", String(!!n.pinned))

      // Title & content
      node.querySelector(".note-title").textContent = n.title
      node.querySelector(".note-content").textContent = n.content

      // Tags - handle both array and comma-separated string
      const tagsEl = node.querySelector(".tags")
      tagsEl.innerHTML = ""
      const tagsArray = Array.isArray(n.tags) ? n.tags : (n.tags || '').split(',').map(t => t.trim()).filter(Boolean);
      tagsArray.forEach((t) => {
        const el = document.createElement("span")
        el.className = "tag"
        el.textContent = `#${t}`
        tagsEl.appendChild(el)
      })

      // Time - handle string timestamps from backend
      const time = node.querySelector(".time")
      const timestamp = n.updatedAt || n.createdAt;
      time.textContent = this.timeAgo(timestamp)
      time.setAttribute("title", new Date(timestamp).toLocaleString())

      // Actions
      node.querySelector(".edit").addEventListener("click", () => this.openEditor(n))
      node.querySelector(".delete").addEventListener("click", () => this.deleteNote(n.id))
      pinBtn.addEventListener("click", () => this.togglePin(n.id))

      // Keyboard open on Enter
      node.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          this.openEditor(n)
        }
      })

      this.els.grid.appendChild(node)
    }

    this.els.grid.setAttribute("aria-busy", "false")
  }

  debounce(fn, ms = 200) {
    let t
    return (...args) => {
      clearTimeout(t)
      t = setTimeout(() => fn(...args), ms)
    }
  }

  bindEvents() {
    // Create
    this.els.newBtn.addEventListener("click", () => this.openEditor())

    // Search with debounce
    this.els.search.addEventListener(
      "input",
      this.debounce((e) => {
        this.state.query = e.target.value
        this.render()
      }, 120),
    )

    // Filter/sort
    this.els.catFilter.addEventListener("change", (e) => {
      this.state.category = e.target.value
      this.render()
    })
    this.els.sortFilter.addEventListener("change", (e) => {
      this.state.sort = e.target.value
      this.render()
    })

    // Editor
    this.els.form.addEventListener("submit", (e) => this.upsertNote(e))
    this.els.cancel.addEventListener("click", () => this.closeEditor())
    this.els.overlay.addEventListener("click", () => this.closeEditor())

    // Keyboard shortcuts
    window.addEventListener("keydown", (e) => {
      if (e.key === "/" && document.activeElement !== this.els.search) {
        e.preventDefault()
        this.els.search.focus()
      }
      if (e.key.toLowerCase() === "n" && !this.els.dialog.hidden) {
        // ignore when dialog open
        return
      }
      if (e.key.toLowerCase() === "n" && e.target.tagName !== "INPUT" && e.target.tagName !== "TEXTAREA") {
        e.preventDefault()
        this.openEditor()
      }
      if (e.key === "Escape" && !this.els.dialog.hidden) {
        this.closeEditor()
      }
    })
  }
}
