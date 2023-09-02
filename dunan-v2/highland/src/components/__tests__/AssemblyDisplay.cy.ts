import AssemblyDisplay from "../catalog/assemblydisplay/AssemblyDisplay.vue"

describe('AssemblyDisplay', () => {
    it('playground', () => {
      cy.mount(AssemblyDisplay)
    })
  
    it('renders properly', () => {
      cy.mount(AssemblyDisplay)
      cy.findByTestId('header-two').should('contain', 'Hello from Assembly Display Component')
    })
  })