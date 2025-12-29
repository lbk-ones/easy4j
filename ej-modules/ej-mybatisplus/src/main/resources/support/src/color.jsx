import React, { createContext, useState, useContext } from 'react';

const ColorContext = createContext();

export function ColorProvider({ children }) {
    const [primaryColor, setPrimaryColor] = useState("#722ed1");

    const value = {
        primaryColor,
        setPrimaryColor,
    };

    return <ColorContext.Provider value={value}>{children}</ColorContext.Provider>;
}

export function useColor() {
    const context = useContext(ColorContext);
    if (context === undefined) {
        throw new Error('useColor must be used within a ColorProvider');
    }
    return context;
}

export default ColorContext;