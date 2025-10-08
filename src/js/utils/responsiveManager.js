/**
 * Responsive Utility Library
 * Handles responsive behaviors across the entire website
 */

export class ResponsiveManager {
    constructor() {
        this.breakpoints = {
            xs: 0,
            sm: 640,
            md: 768,
            lg: 1024,
            xl: 1280,
            '2xl': 1536
        };
        
        this.currentBreakpoint = this.getCurrentBreakpoint();
        this.listeners = [];
    }

    getCurrentBreakpoint() {
        const width = window.innerWidth;
        
        if (width >= this.breakpoints['2xl']) return '2xl';
        if (width >= this.breakpoints.xl) return 'xl';
        if (width >= this.breakpoints.lg) return 'lg';
        if (width >= this.breakpoints.md) return 'md';
        if (width >= this.breakpoints.sm) return 'sm';
        return 'xs';
    }

    isMobile() {
        return window.innerWidth < this.breakpoints.md;
    }

    isTablet() {
        return window.innerWidth >= this.breakpoints.md && window.innerWidth < this.breakpoints.lg;
    }

    isDesktop() {
        return window.innerWidth >= this.breakpoints.lg;
    }

    onBreakpointChange(callback) {
        this.listeners.push(callback);
        
        let resizeTimer;
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(() => {
                const newBreakpoint = this.getCurrentBreakpoint();
                if (newBreakpoint !== this.currentBreakpoint) {
                    const oldBreakpoint = this.currentBreakpoint;
                    this.currentBreakpoint = newBreakpoint;
                    callback(newBreakpoint, oldBreakpoint);
                }
            }, 150);
        });
    }

    // Touch detection
    isTouchDevice() {
        return ('ontouchstart' in window) || 
               (navigator.maxTouchPoints > 0) || 
               (navigator.msMaxTouchPoints > 0);
    }

    // Orientation detection
    getOrientation() {
        return window.innerHeight > window.innerWidth ? 'portrait' : 'landscape';
    }

    onOrientationChange(callback) {
        window.addEventListener('orientationchange', () => {
            setTimeout(() => {
                callback(this.getOrientation());
            }, 100);
        });
        
        // Fallback for browsers without orientationchange
        let lastOrientation = this.getOrientation();
        window.addEventListener('resize', () => {
            const newOrientation = this.getOrientation();
            if (newOrientation !== lastOrientation) {
                lastOrientation = newOrientation;
                callback(newOrientation);
            }
        });
    }

    // Viewport utilities
    getViewportWidth() {
        return Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
    }

    getViewportHeight() {
        return Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
    }

    // Scroll lock (useful for modals/sidebars on mobile)
    lockScroll() {
        document.body.style.overflow = 'hidden';
        document.body.style.position = 'fixed';
        document.body.style.width = '100%';
    }

    unlockScroll() {
        document.body.style.overflow = '';
        document.body.style.position = '';
        document.body.style.width = '';
    }

    // Safe area handling for iOS notch
    getSafeAreaInsets() {
        const style = getComputedStyle(document.documentElement);
        return {
            top: parseInt(style.getPropertyValue('--sat') || '0'),
            right: parseInt(style.getPropertyValue('--sar') || '0'),
            bottom: parseInt(style.getPropertyValue('--sab') || '0'),
            left: parseInt(style.getPropertyValue('--sal') || '0')
        };
    }

    // Apply responsive classes based on breakpoint
    applyResponsiveClasses() {
        const bp = this.getCurrentBreakpoint();
        document.body.setAttribute('data-breakpoint', bp);
        
        // Add helper classes
        document.body.classList.toggle('is-mobile', this.isMobile());
        document.body.classList.toggle('is-tablet', this.isTablet());
        document.body.classList.toggle('is-desktop', this.isDesktop());
        document.body.classList.toggle('is-touch', this.isTouchDevice());
    }

    // Debounce utility
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Throttle utility
    throttle(func, limit) {
        let inThrottle;
        return function(...args) {
            if (!inThrottle) {
                func.apply(this, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    // Match media query
    matchMedia(query) {
        return window.matchMedia(query).matches;
    }

    // Initialize responsive behaviors
    initialize() {
        this.applyResponsiveClasses();
        
        // Update on resize
        window.addEventListener('resize', this.debounce(() => {
            this.applyResponsiveClasses();
        }, 150));

        // Add CSS custom properties for viewport height (fixes iOS Safari issues)
        const setVH = () => {
            const vh = window.innerHeight * 0.01;
            document.documentElement.style.setProperty('--vh', `${vh}px`);
        };
        
        setVH();
        window.addEventListener('resize', this.debounce(setVH, 150));
        window.addEventListener('orientationchange', setVH);

        // Set safe area insets
        if (CSS.supports('padding-top: env(safe-area-inset-top)')) {
            document.documentElement.style.setProperty('--sat', 'env(safe-area-inset-top)');
            document.documentElement.style.setProperty('--sar', 'env(safe-area-inset-right)');
            document.documentElement.style.setProperty('--sab', 'env(safe-area-inset-bottom)');
            document.documentElement.style.setProperty('--sal', 'env(safe-area-inset-left)');
        }

        console.log('ResponsiveManager initialized', {
            breakpoint: this.currentBreakpoint,
            isMobile: this.isMobile(),
            isTablet: this.isTablet(),
            isDesktop: this.isDesktop(),
            isTouchDevice: this.isTouchDevice(),
            orientation: this.getOrientation()
        });
    }
}

// Create singleton instance
export const responsiveManager = new ResponsiveManager();
