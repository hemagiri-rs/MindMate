import { ComponentLoader } from "./componentLoader.js"
import { apiService } from "./apiService.js"

export class Router {
  constructor() {
    this.routes = new Map()
    this.currentRoute = null
    this.defaultRoute = null

    // Handle browser navigation events
    window.addEventListener("popstate", (e) => this.handleRoute(e.state?.route))
  }

  addRoute(path, component, options = {}) {
    this.routes.set(path, { component, ...options })
    if (options.default) {
      this.defaultRoute = path
    }
  }

  async navigate(path, data = {}) {
    const route = this.routes.get(path)
    if (!route) {
      console.error(`Route not found: ${path}`)
      return false
    }

    // Check authentication for protected routes
    if (this.isProtectedRoute(path) && !apiService.isAuthenticated()) {
      console.log("Redirecting to auth - user not authenticated")
      return this.navigate("/auth")
    }

    // Redirect authenticated users away from auth page
    if (path === "/auth" && apiService.isAuthenticated()) {
      console.log("Redirecting to dashboard - user already authenticated")
      return this.navigate("/dashboard")
    }

    // Update browser history
    window.history.pushState({ route: path }, "", path)

    // Handle the route change
    return this.handleRoute(path, data)
  }

  isProtectedRoute(path) {
    const protectedRoutes = ["/dashboard", "/notes", "/tasks", "/mood", "/chatbot"]
    return protectedRoutes.includes(path)
  }

  async handleRoute(path, data = {}) {
    const route = this.routes.get(path) || this.routes.get(this.defaultRoute)
    if (!route) return false

    // Clear previous route if needed
    if (this.currentRoute && this.currentRoute.onLeave) {
      await this.currentRoute.onLeave()
    }

    // Load and render the new component
    const success = await ComponentLoader.renderComponent(route.component, "#app-content", data)

    if (success) {
      // Load sidebar for dashboard pages
      if (["dashboard", "notes", "tasks", "mood", "chatbot"].includes(route.component)) {
        await this.loadSidebar()
        this.updateActiveNavItem(route.component)
        // Add class to body to indicate sidebar is present
        document.body.classList.add("has-sidebar")
      } else {
        // Remove sidebar class when not on dashboard pages
        document.body.classList.remove("has-sidebar")
      }

      // Initialize component-specific functionality
      this.initializeComponent(route.component)

      if (route.onEnter) {
        await route.onEnter(data)
      }
    }

    this.currentRoute = route
    return success
  }

  updateActiveNavItem(currentComponent) {
    // Remove active from all, clear aria-current
    document.querySelectorAll(".nav-item").forEach((item) => {
      item.classList.remove("active")
      item.removeAttribute("aria-current")
    })

    // Add active to current item and set aria-current="page"
    const activeItem = document.querySelector(`.nav-item[data-route="${currentComponent}"]`)
    if (activeItem) {
      activeItem.classList.add("active")
      activeItem.setAttribute("aria-current", "page")
    }
  }

  async loadSidebar() {
    const sidebarContainer = document.getElementById("sidebar-container")
    if (sidebarContainer) {
      const sidebarHtml = await ComponentLoader.loadComponent("sidebar")
      if (sidebarHtml) {
        // Get user data from API service
        const userData = apiService.getCurrentUser()

        const processedHtml = sidebarHtml
          .replace(/\${userInitial}/g, userData?.name?.charAt(0) || "U")
          .replace(/\${userName}/g, userData?.name || "User")
          .replace(/\${userEmail}/g, userData?.email || "user@example.com")

        sidebarContainer.innerHTML = processedHtml

        // Initialize sidebar manager after sidebar HTML is loaded
        if (window.app && window.app.sidebarManager) {
          window.app.sidebarManager.initialize()
        }

        // Initialize mobile sidebar functionality
        this.initMobileSidebar()

        // Add logout functionality
        const logoutBtn = document.getElementById("logout-btn")
        if (logoutBtn) {
          logoutBtn.addEventListener("click", (e) => {
            e.preventDefault()
            this.logout()
          })
        }
      }
    }
  }

  initMobileSidebar() {
    const sidebar = document.getElementById("sidebar")
    const sidebarOverlay = document.getElementById("sidebar-overlay")
    const sidebarToggle = document.getElementById("sidebar-toggle")
    const mobileMenuBtn = document.getElementById("mobile-menu-btn")

    const openSidebar = () => {
      if (sidebar) sidebar.classList.add("active")
      if (sidebarOverlay) sidebarOverlay.classList.add("active")
    }

    const closeSidebar = () => {
      if (sidebar) sidebar.classList.remove("active")
      if (sidebarOverlay) sidebarOverlay.classList.remove("active")
    }

    if (mobileMenuBtn) mobileMenuBtn.addEventListener("click", openSidebar)
    if (sidebarToggle) sidebarToggle.addEventListener("click", closeSidebar)
    if (sidebarOverlay) sidebarOverlay.addEventListener("click", closeSidebar)

    // Close on nav click (mobile only)
    document.querySelectorAll(".nav-item").forEach((item) => {
      item.addEventListener("click", () => {
        if (window.innerWidth < 768) closeSidebar()
      })
    })

    window.addEventListener("resize", () => {
      if (window.innerWidth >= 768) closeSidebar()
    })
  }

  logout() {
    apiService.logout()
    this.navigate("/auth")
  }

  initializeComponent(componentName) {
    // Initialize component-specific functionality after DOM is loaded
    switch (componentName) {
      case "auth":
        if (window.app && window.app.auth) {
          window.app.auth.initialize()
        }
        break
      case "dashboard":
        if (window.app && window.app.dashboard) {
          window.app.dashboard.initialize()
        }
        break
      case "notes":
        if (window.app && window.app.notes) {
          window.app.notes.initialize()
        }
        break
      case "tasks":
        if (window.app && window.app.tasks) {
          window.app.tasks.initialize()
        }
        break
      case "mood":
        if (window.app && window.app.mood) {
          window.app.mood.initialize()
        }
        break
      case "chatbot":
        if (window.app && window.app.chatbot) {
          window.app.chatbot.initialize()
        }
        break
    }
  }

  start() {
    // Handle initial route
    const path = window.location.pathname
    const route = this.routes.get(path)

    // Check authentication status first
    if (this.isProtectedRoute(path) && !apiService.isAuthenticated()) {
      this.handleRoute("/auth")
      return
    }

    if (path === "/auth" && apiService.isAuthenticated()) {
      this.handleRoute("/dashboard")
      return
    }

    if (route) {
      this.handleRoute(path)
    } else if (this.defaultRoute) {
      this.handleRoute(this.defaultRoute)
    }
  }
}
