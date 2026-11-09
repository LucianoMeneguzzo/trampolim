import '@testing-library/jest-dom'
import { configure } from '@testing-library/react'

// findBy... e waitFor esperam até 5s (o padrão é 1s): em máquina ocupada a tela demora mais para montar
configure({ asyncUtilTimeout: 5000 })
