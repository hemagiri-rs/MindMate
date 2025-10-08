export class ComponentLoader {
    static async loadComponent(name) {
        try {
            const response = await fetch(`/components/${name}.html`);
            if (!response.ok) {
                throw new Error(`Failed to load component ${name}`);
            }
            return await response.text();
        } catch (error) {
            console.error(`Error loading component ${name}:`, error);
            return null;
        }
    }

    static async renderComponent(name, targetElement, data = {}) {
        const html = await this.loadComponent(name);
        if (!html) return false;

        // Process any template variables
        const processedHtml = this.processTemplate(html, data);
        
        // Insert the component HTML
        if (typeof targetElement === 'string') {
            targetElement = document.querySelector(targetElement);
        }
        if (targetElement) {
            targetElement.innerHTML = processedHtml;
            return true;
        }
        return false;
    }

    static processTemplate(template, data) {
        return template.replace(/\${(\w+)}/g, (match, key) => {
            return data[key] || '';
        });
    }
}
