/**
 * Sidebar Manager - Handles resizing, mobile toggle, and responsive behavior
 */
export class SidebarManager {
    constructor() {
        this.sidebar = null;
        this.resizeHandle = null;
        this.mobileToggle = null;
        this.mobileClose = null;
        this.appContent = null;
        
        this.isResizing = false;
        this.startX = 0;
        this.startWidth = 0;
        
        this.minWidth = 200;
        this.maxWidth = 400;
        this.defaultWidth = 280;
        
        this.breakpoint = 768; // md breakpoint
    }

    initialize() {
        this.sidebar = document.getElementById('sidebar');
        this.resizeHandle = document.getElementById('resize-handle');
        this.mobileToggle = document.getElementById('mobile-toggle');
        this.mobileClose = document.getElementById('mobile-close');
        this.appContent = document.getElementById('app-content');

        if (!this.sidebar) {
            console.error('Sidebar element not found');
            return;
        }

        this.setupResize();
        this.setupMobileToggle();
        this.setupActiveNavigation();
        this.restoreSavedWidth();
        this.setupResponsiveLayout();
    }

    setupResize() {
        if (!this.resizeHandle) return;

        this.resizeHandle.addEventListener('mousedown', (e) => {
            if (window.innerWidth < this.breakpoint) return;
            
            this.isResizing = true;
            this.startX = e.clientX;
            this.startWidth = this.sidebar.offsetWidth;
            
            document.body.style.cursor = 'col-resize';
            document.body.style.userSelect = 'none';
            
            e.preventDefault();
        });

        document.addEventListener('mousemove', (e) => {
            if (!this.isResizing) return;

            const delta = e.clientX - this.startX;
            const newWidth = Math.max(this.minWidth, Math.min(this.maxWidth, this.startWidth + delta));
            
            this.setSidebarWidth(newWidth);
        });

        document.addEventListener('mouseup', () => {
            if (this.isResizing) {
                this.isResizing = false;
                document.body.style.cursor = '';
                document.body.style.userSelect = '';
                
                // Save width
                localStorage.setItem('sidebar-width', this.sidebar.offsetWidth);
            }
        });

        // Touch support for tablets
        this.resizeHandle.addEventListener('touchstart', (e) => {
            if (window.innerWidth < this.breakpoint) return;
            
            this.isResizing = true;
            this.startX = e.touches[0].clientX;
            this.startWidth = this.sidebar.offsetWidth;
            
            e.preventDefault();
        });

        document.addEventListener('touchmove', (e) => {
            if (!this.isResizing) return;

            const delta = e.touches[0].clientX - this.startX;
            const newWidth = Math.max(this.minWidth, Math.min(this.maxWidth, this.startWidth + delta));
            
            this.setSidebarWidth(newWidth);
        });

        document.addEventListener('touchend', () => {
            if (this.isResizing) {
                this.isResizing = false;
                localStorage.setItem('sidebar-width', this.sidebar.offsetWidth);
            }
        });
    }

    setSidebarWidth(width) {
        this.sidebar.style.width = `${width}px`;
    }

    setupMobileToggle() {
        if (this.mobileToggle) {
            this.mobileToggle.addEventListener('click', (e) => {
                e.stopPropagation();
                this.openSidebar();
            });
        }

        if (this.mobileClose) {
            this.mobileClose.addEventListener('click', (e) => {
                e.stopPropagation();
                this.closeSidebar();
            });
        }

        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', (e) => {
            if (window.innerWidth >= this.breakpoint) return;
            
            if (this.sidebar && 
                !this.sidebar.contains(e.target) && 
                !this.mobileToggle?.contains(e.target) &&
                !this.sidebar.classList.contains('-translate-x-full')) {
                this.closeSidebar();
            }
        });

        // Close sidebar on escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && window.innerWidth < this.breakpoint) {
                this.closeSidebar();
            }
        });
    }

    openSidebar() {
        this.sidebar.classList.remove('-translate-x-full');
        document.body.style.overflow = 'hidden'; // Prevent scroll on mobile
    }

    closeSidebar() {
        this.sidebar.classList.add('-translate-x-full');
        document.body.style.overflow = '';
    }

    setupActiveNavigation() {
        const updateActive = () => {
            const currentPath = window.location.hash.slice(1) || '/dashboard';
            const navItems = document.querySelectorAll('.nav-item');
            
            navItems.forEach(item => {
                const href = item.getAttribute('href');
                if (href === currentPath) {
                    item.classList.add('active');
                    item.style.background = 'rgba(16, 185, 129, 0.15)';
                    item.style.borderLeft = '3px solid rgb(16, 185, 129)';
                } else {
                    item.classList.remove('active');
                    item.style.background = '';
                    item.style.borderLeft = '';
                }
            });
        };

        updateActive();
        window.addEventListener('hashchange', updateActive);
        window.addEventListener('popstate', updateActive);
    }

    restoreSavedWidth() {
        const savedWidth = localStorage.getItem('sidebar-width');
        if (savedWidth && window.innerWidth >= this.breakpoint) {
            const width = parseInt(savedWidth);
            if (width >= this.minWidth && width <= this.maxWidth) {
                this.setSidebarWidth(width);
            }
        }
    }

    setupResponsiveLayout() {
        const handleResize = () => {
            if (window.innerWidth >= this.breakpoint) {
                // Desktop: ensure sidebar is visible and positioned correctly
                this.sidebar.classList.remove('-translate-x-full');
                document.body.style.overflow = '';
                
                // Restore saved width
                this.restoreSavedWidth();
            } else {
                // Mobile: hide sidebar by default
                this.sidebar.classList.add('-translate-x-full');
                
                // Reset to default width
                this.setSidebarWidth(this.defaultWidth);
            }
        };

        // Initial setup
        handleResize();

        // Listen for window resize with debouncing
        let resizeTimer;
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(handleResize, 100);
        });
    }

    // Public methods
    toggle() {
        if (this.sidebar.classList.contains('-translate-x-full')) {
            this.openSidebar();
        } else {
            this.closeSidebar();
        }
    }

    isOpen() {
        return !this.sidebar.classList.contains('-translate-x-full');
    }

    getWidth() {
        return this.sidebar.offsetWidth;
    }

    setWidth(width) {
        const newWidth = Math.max(this.minWidth, Math.min(this.maxWidth, width));
        this.setSidebarWidth(newWidth);
        localStorage.setItem('sidebar-width', newWidth);
    }

    reset() {
        this.setWidth(this.defaultWidth);
    }
}

// Create singleton instance
export const sidebarManager = new SidebarManager();
